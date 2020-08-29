package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeyProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * @author Deolin 2020-07-19
 */
public class QueryByKeyXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final Collection<QueryByKeyProc> queryByKeyProcs;

    @Getter
    private final Collection<String> sourceCodeLines = Lists.newArrayList();

    public QueryByKeyXmlProc(PersistenceDto persistence, Collection<QueryByKeyProc> queryByKeyProcs) {
        this.persistence = persistence;
        this.queryByKeyProcs = queryByKeyProcs;
    }

    public QueryByKeyXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByKey()) {
            return this;
        }
        for (QueryByKeyProc queryByKeyProc : queryByKeyProcs) {
            Element stmt = new DefaultElement("select");
            stmt.addAttribute("id", queryByKeyProc.getMethodName());
            addParameterType(stmt, queryByKeyProc.getKey());
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
                stmt.addText(PersistenceGeneratorConfig.getInstance().getNotDeletedSql());
                newLineWithIndent(stmt);
                stmt.addText("AND ");
            }
            stmt.addText(queryByKeyProc.getKey().getColumnName() + " = #{" + queryByKeyProc.getKey().getPropertyName()
                    + "}");
            sourceCodeLines.addAll(Dom4jUtils.toSourceCodeLines(stmt));
        }
        return this;
    }

}