package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-19
 */
public class QueryByPkXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByPkXmlProc(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public QueryByPkXmlProc process() {
        if (persistence.getPkProperties().size() > 0) {
            Element stmt = new DefaultElement("select");
            stmt.addAttribute("id", "queryById");
            if (persistence.getPkProperties().size() == 1) {
                addParameterType(stmt, Iterables.getOnlyElement(persistence.getPkProperties()));
            }
            stmt.addAttribute("resultMap", "all");
            newLineWithIndent(stmt);
            stmt.addText("SELECT");
            stmt.addElement("include").addAttribute("refid", "all");
            newLineWithIndent(stmt);
            stmt.addText("FROM ").addText(persistence.getTableName());
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
                    .collect(Collectors.joining(", ")));
            sourceCodeLines = StringUtils.splitLineByLine(Dom4jUtils.toSourceCode(stmt));
        }
        return this;
    }

}