package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsEachIdProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsProc;

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
            List<String> xmlLines = Lists.newArrayList();
            String methodName = null;
            if (queryByIdsProc != null) {
                methodName = queryByIdsProc.getMethodName();
            }
            if (queryByIdsEachIdProc != null) {
                methodName = queryByIdsEachIdProc.getMethodName();
            }
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName,
                    onlyPk.getJavaType().getName().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<!-- @formatter:off -->");
            xmlLines.add(BaseConstant.SINGLE_INDENT +"SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGeneratorConfig.getInstance()
                        .getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + onlyPk.getColumnName() + " IN (<foreach collection=\"ids\" item=\"one\" separator=\",\">#{one}</foreach>)");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<!-- @formatter:on -->");
            xmlLines.add("</select>");
            sourceCodeLines = xmlLines;
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}