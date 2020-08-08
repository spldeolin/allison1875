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
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByPkXmlProc implements XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByPkXmlProc(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public UpdateByPkXmlProc process() {
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
            if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                updateByIdTag.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                updateByIdTag.addText(Constant.newLine).addText(Constant.singleIndent);
                updateByIdTag.addText("AND ");
            }
            updateByIdTag.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(updateByIdTag));
        }
        return this;
    }

}