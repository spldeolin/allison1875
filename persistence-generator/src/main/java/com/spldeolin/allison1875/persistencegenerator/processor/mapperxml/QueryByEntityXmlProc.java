package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
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

    public QueryByEntityXmlProc(PersistenceDto persistence, QueryByEntityProc queryByEntityProc, String entityName) {
        this.persistence = persistence;
        this.queryByEntityProc = queryByEntityProc;
        this.entityName = entityName;
    }

    public QueryByEntityXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryById()) {
            return this;
        }

        sourceCodeLines = Lists.newArrayList();
        sourceCodeLines.add(String
                .format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", queryByEntityProc.getMethodName(),
                        entityName));
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "<include refid=\"all\"/>");
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            sourceCodeLines
                    .add(BaseConstant.SINGLE_INDENT + PersistenceGeneratorConfig.getInstance().getNotDeletedSql());
        }
        for (PropertyDto property : persistence.getProperties()) {
            sourceCodeLines.add(BaseConstant.SINGLE_INDENT + String
                    .format("<if test=\"%s!=null\"> AND %s = #{%s} </if>", property.getPropertyName(),
                            property.getColumnName(), property.getPropertyName()));
        }
        sourceCodeLines.add("</select>");

        return this;
    }

    @Override
    public Collection<String> getSourceCodeLines() {
        return sourceCodeLines;
    }

}