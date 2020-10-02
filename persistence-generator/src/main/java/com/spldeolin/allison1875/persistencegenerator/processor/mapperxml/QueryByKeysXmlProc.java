package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeysProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;

/**
 * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * @author Deolin 2020-07-19
 */
public class QueryByKeysXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final Collection<QueryByKeysProc> queryByKeysProcs;

    private final Collection<String> sourceCodeLines = Lists.newArrayList();

    public QueryByKeysXmlProc(PersistenceDto persistence, Collection<QueryByKeysProc> queryByKeysProcs) {
        this.persistence = persistence;
        this.queryByKeysProcs = queryByKeysProcs;
    }

    public QueryByKeysXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByKeys()) {
            return this;
        }
        for (QueryByKeysProc queryByKeyProc : queryByKeysProcs) {
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
            stmt.addText(queryByKeyProc.getKey().getColumnName()).addText(" IN (");
            stmt.addElement("foreach").addAttribute("collection", queryByKeyProc.getVarsName())
                    .addAttribute("item", "one").addAttribute("separator", ",").addText("#{one}");
            stmt.addText(")");
            sourceCodeLines.addAll(Dom4jUtils.toSourceCodeLines(stmt));
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}