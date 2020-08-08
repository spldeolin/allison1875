package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 删除可能存在的update(id=updateById)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdXmlProcessor implements SourceCodeGetter {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByIdXmlProcessor(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public UpdateByIdXmlProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            Element updateByIdTag = new DefaultElement("update");
            updateByIdTag.addAttribute("id", "updateById");
            updateByIdTag.addAttribute("parameterType", entityName);

            updateByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdTag.addText("UPDATE ").addText(persistence.getTableName());
            Element setTag = updateByIdTag.addElement("set");
            for (PropertyDto nonPk : persistence.getNonPkProperties()) {
                Element ifTag = setTag.addElement("if");
                ifTag.addAttribute("test", nonPk.getPropertyName() + "!=null");
                ifTag.addText(Constant.newLine).addText(Constant.trebleIndex);
                ifTag.addText(nonPk.getColumnName() + " = #{" + nonPk.getPropertyName() + "},");
                ifTag.addText(Constant.newLine).addText(Constant.doubleIndex);
            }
            updateByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdTag.addText("WHERE ");
            updateByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdTag.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
            updateByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdTag.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(updateByIdTag));
        }
        return this;
    }

}