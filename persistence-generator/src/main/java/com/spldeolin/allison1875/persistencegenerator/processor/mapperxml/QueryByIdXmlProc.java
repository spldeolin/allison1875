package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdProc;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-19
 */
public class QueryByIdXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final QueryByIdProc queryByIdProc;

    private Collection<String> sourceCodeLines;

    public QueryByIdXmlProc(PersistenceDto persistence, QueryByIdProc queryByIdProc) {
        this.persistence = persistence;
        this.queryByIdProc = queryByIdProc;
    }

    public QueryByIdXmlProc process() {
        if (PersistenceGenerator.CONFIG.get().getDisableQueryById()) {
            return this;
        }
        if (persistence.getIdProperties().size() > 0) {
            List<String> xmlLines = Lists.newArrayList();
            String firstLine = "<select id=\"" + queryByIdProc.getMethodName() + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getName().replaceFirst("java\\.lang\\.", "") + "\" ";
            }
            firstLine += "resultMap=\"all\">";
            xmlLines.add(firstLine);
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(
                        BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGenerator.CONFIG.get().getNotDeletedSql());
            }
            for (PropertyDto idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + idProperty.getColumnName() + " = #{" + idProperty
                        .getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            sourceCodeLines = xmlLines;
            sourceCodeLines.add("");
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}