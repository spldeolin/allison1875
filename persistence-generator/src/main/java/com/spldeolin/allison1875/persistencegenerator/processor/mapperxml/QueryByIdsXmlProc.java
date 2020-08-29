package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsEachIdProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;

/**
 * 这个Proc生成2中方法：
 * 1. 根据主键列表查询
 * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdsXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final QueryByIdsProc queryByIdsProc;

    private final QueryByIdsEachIdProc queryByIdsEachIdProc;

    @Getter
    private Collection<String> sourceCodeLines;

    public QueryByIdsXmlProc(PersistenceDto persistence, QueryByIdsEachIdProc queryByIdsEachPkProc) {
        this.persistence = persistence;
        this.queryByIdsProc = null;
        this.queryByIdsEachIdProc = queryByIdsEachPkProc;
    }

    public QueryByIdsXmlProc(PersistenceDto persistence, QueryByIdsProc queryByPksProc) {
        this.persistence = persistence;
        this.queryByIdsProc = queryByPksProc;
        this.queryByIdsEachIdProc = null;
    }


    public QueryByIdsXmlProc process() {
        if (persistence.getIdProperties().size() == 1) {
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            Element stmt = new DefaultElement("select");
            String methodName = null;
            if (queryByIdsProc != null) {
                if (PersistenceGeneratorConfig.getInstance().getDisableQueryByIds()) {
                    return this;
                }
                methodName = queryByIdsProc.getMethodName();
            }
            if (queryByIdsEachIdProc != null) {
                if (PersistenceGeneratorConfig.getInstance().getDisableQueryByIdsEachId()) {
                    return this;
                }
                methodName = queryByIdsEachIdProc.getMethodName();
            }
            stmt.addAttribute("id", methodName);
            addParameterType(stmt, onlyPk);
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
            stmt.addText(onlyPk.getColumnName()).addText(" IN (");
            stmt.addElement("foreach").addAttribute("collection", "ids").addAttribute("item", "one")
                    .addAttribute("separator", ",").addText("#{one}");
            stmt.addText(")");
            sourceCodeLines = Dom4jUtils.toSourceCodeLines(stmt);
        }
        return this;
    }

}