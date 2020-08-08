package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 删除可能存在的update(id=updateByIdEvenNull)标签，并重新生成
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdEvenNullXmlProcessor implements SourceCodeGetter {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByIdEvenNullXmlProcessor(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public UpdateByIdEvenNullXmlProcessor process() {
        if (persistence.getPkProperties().size() > 0) {
            Element updateByIdEvenNullTag = new DefaultElement("update");
            updateByIdEvenNullTag.addAttribute("id", "updateByIdEvenNull");
            updateByIdEvenNullTag.addAttribute("parameterType", entityName);
            updateByIdEvenNullTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdEvenNullTag.addText("UPDATE ").addText(persistence.getTableName());
            updateByIdEvenNullTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdEvenNullTag.addText("SET ");
            updateByIdEvenNullTag.addText(persistence.getNonPkProperties().stream()
                    .map(npk -> npk.getColumnName() + "=#{" + npk.getPropertyName() + "}")
                    .collect(Collectors.joining(", ")));
            updateByIdEvenNullTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdEvenNullTag.addText("WHERE ");
            updateByIdEvenNullTag.addText(Constant.newLine).addText(Constant.singleIndent);
            updateByIdEvenNullTag.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + "=#{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(updateByIdEvenNullTag));
        }
        return this;
    }

}