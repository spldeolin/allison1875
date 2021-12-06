package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2021-01-03
 */
public class ListAllXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableListAll()) {
            return null;
        }
        Collection<String> result = Lists.newArrayList();
        String firstLine = "<select id=\"" + methodName + "\" ";
        firstLine += "resultMap=\"all\">";
        result.add(firstLine);
        result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        result.add(BaseConstant.SINGLE_INDENT + "SELECT");
        result.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        result.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        result.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            result.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        result.add("</select>");
        result.add("");
        return result;
    }

}