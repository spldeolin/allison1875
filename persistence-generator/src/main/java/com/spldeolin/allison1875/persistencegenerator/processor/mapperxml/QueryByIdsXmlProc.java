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
 * 这个Proc生成2中方法：
 * 1. 根据主键列表查询
 * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class QueryByIdsXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() == 1) {
            PropertyDto onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName,
                    onlyPk.getJavaType().getName().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + onlyPk.getColumnName()
                    + " IN (<foreach collection=\"ids\" item=\"one\" separator=\",\">#{one}</foreach>)");
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

}