package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 根据主键查询
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class QueryByIdXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableQueryById()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() > 0) {
            String firstLine = "<select id=\"" + methodName + "\" ";
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
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            for (PropertyDto idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + idProperty.getColumnName() + " = #{" + idProperty
                        .getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

}