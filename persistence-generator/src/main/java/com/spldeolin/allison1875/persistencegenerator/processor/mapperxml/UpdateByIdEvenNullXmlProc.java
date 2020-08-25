package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.base.Strings;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdEvenNullProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdEvenNullXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private final UpdateByIdEvenNullProc updateByIdEvenNullProc;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByIdEvenNullXmlProc(PersistenceDto persistence, String entityName,
            UpdateByIdEvenNullProc updateByPkEvenNullProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.updateByIdEvenNullProc = updateByPkEvenNullProc;
    }

    public UpdateByIdEvenNullXmlProc process() {
        if (PersistenceGeneratorConfig.getInstace().getDisableUpdateByIdEvenNull()) {
            return this;
        }
        if (persistence.getIdProperties().size() > 0) {
            Element stmt = new DefaultElement("update");
            stmt.addAttribute("id", updateByIdEvenNullProc.getMethodName());
            stmt.addAttribute("parameterType", entityName);
            newLineWithIndent(stmt);
            stmt.addText("UPDATE ").addText(persistence.getTableName());
            newLineWithIndent(stmt);
            stmt.addText("SET ");
            newLineWithIndent(stmt);
            stmt.addText(BaseConstant.SINGLE_INDENT);
            stmt.addText(persistence.getNonIdProperties().stream()
                    .map(npk -> npk.getColumnName() + " = #{" + npk.getPropertyName() + "}").collect(Collectors
                            .joining(", " + BaseConstant.NEW_LINE + Strings.repeat(BaseConstant.SINGLE_INDENT, 2))));
            newLineWithIndent(stmt);
            stmt.addText("WHERE");
            newLineWithIndent(stmt);
            if (persistence.getIsDeleteFlagExist()) {
                stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(persistence.getIdProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = Dom4jUtils.toSourceCodeLines(stmt);
        }
        return this;
    }

}