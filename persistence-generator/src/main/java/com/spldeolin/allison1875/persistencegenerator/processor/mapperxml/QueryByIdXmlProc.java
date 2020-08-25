package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.stream.Collectors;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final QueryByIdProc queryByIdProc;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByIdXmlProc(PersistenceDto persistence, QueryByIdProc queryByIdProc) {
        this.persistence = persistence;
        this.queryByIdProc = queryByIdProc;
    }

    public QueryByIdXmlProc process() {
        if (PersistenceGeneratorConfig.getInstace().getDisableQueryById()) {
            return this;
        }
        if (persistence.getIdProperties().size() > 0) {
            Element stmt = new DefaultElement("select");
            stmt.addAttribute("id", queryByIdProc.getMethodName());
            if (persistence.getIdProperties().size() == 1) {
                addParameterType(stmt, Iterables.getOnlyElement(persistence.getIdProperties()));
            }
            stmt.addAttribute("resultMap", "all");
            newLineWithIndent(stmt);
            stmt.addText("SELECT");
            stmt.addElement("include").addAttribute("refid", "all");
            newLineWithIndent(stmt);
            stmt.addText("FROM ").addText(persistence.getTableName());
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
                    .collect(Collectors.joining(", ")));
            sourceCodeLines = Dom4jUtils.toSourceCodeLines(stmt);
        }
        return this;
    }

}