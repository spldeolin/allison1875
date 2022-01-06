package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2020-10-27
 */
@Singleton
public class QueryByEntityXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String entityName, String methodName) {
        if (persistenceGeneratorConfig.getDisableQueryByEntity()) {
            return null;
        }

        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(
                String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName, entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("  <if test=\"%s!=null\"> AND `%s` = #{%s} </if>",
                    property.getPropertyName(), property.getColumnName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</select>");
        xmlLines.add("");
        return xmlLines;
    }

}