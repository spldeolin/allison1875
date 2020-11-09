package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByEntityProc;

/**
 * @author Deolin 2020-10-27
 */
public class QueryByEntityXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final QueryByEntityProc queryByEntityProc;

    private final String entityName;

    private Collection<String> sourceCodeLines;

    public QueryByEntityXmlProc(PersistenceDto persistence, String entityName, QueryByEntityProc queryByEntityProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.queryByEntityProc = queryByEntityProc;
    }

    public QueryByEntityXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByEntity()) {
            return this;
        }

        List<String> xmlLines = Lists.newArrayList();
        xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                queryByEntityProc.getMethodName(), entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
        xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGeneratorConfig.getInstance()
                    .getNotDeletedSql());
        }
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String
                    .format("  <if test=\"%s!=null\"> AND %s = #{%s} </if>", property.getPropertyName(),
                            property.getColumnName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
        xmlLines.add("</select>");
        sourceCodeLines = xmlLines;
        sourceCodeLines.add("");
        return this;
    }

    @Override
    public Collection<String> getSourceCodeLines() {
        return sourceCodeLines;
    }

}