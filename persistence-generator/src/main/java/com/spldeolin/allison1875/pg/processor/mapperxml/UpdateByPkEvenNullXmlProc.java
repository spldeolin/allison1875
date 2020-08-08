package com.spldeolin.allison1875.pg.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByPkEvenNullXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    @Getter
    private Collection<String> sourceCodeLines;

    public UpdateByPkEvenNullXmlProc(PersistenceDto persistence, String entityName) {
        this.persistence = persistence;
        this.entityName = entityName;
    }

    public UpdateByPkEvenNullXmlProc process() {
        if (persistence.getPkProperties().size() > 0) {
            Element stmt = new DefaultElement("update");
            stmt.addAttribute("id", "updateByIdEvenNull");
            stmt.addAttribute("parameterType", entityName);
            newLineWithIndent(stmt);
            stmt.addText("UPDATE ").addText(persistence.getTableName());
            newLineWithIndent(stmt);
            stmt.addText("SET ");
            stmt.addText(persistence.getNonPkProperties().stream()
                    .map(npk -> npk.getColumnName() + " = #{" + npk.getPropertyName() + "}")
                    .collect(Collectors.joining(", ")));
            newLineWithIndent(stmt);
            stmt.addText("WHERE ");
            newLineWithIndent(stmt);
            if (PersistenceGeneratorConfig.getInstace().getNotDeletedSql() != null) {
                stmt.addText(PersistenceGeneratorConfig.getInstace().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(persistence.getPkProperties().stream()
                    .map(pk -> pk.getColumnName() + " = #{" + pk.getPropertyName() + "}")
                    .collect(Collectors.joining(" AND ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt));
        }
        return this;
    }

}