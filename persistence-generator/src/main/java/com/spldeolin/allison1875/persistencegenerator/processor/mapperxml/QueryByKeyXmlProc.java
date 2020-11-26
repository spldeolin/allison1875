package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeyProc;

/**
 * 根据外键查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * @author Deolin 2020-07-19
 */
public class QueryByKeyXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final Collection<QueryByKeyProc> queryByKeyProcs;

    private final Collection<String> sourceCodeLines = Lists.newArrayList();

    public QueryByKeyXmlProc(PersistenceDto persistence, Collection<QueryByKeyProc> queryByKeyProcs) {
        this.persistence = persistence;
        this.queryByKeyProcs = queryByKeyProcs;
    }

    public QueryByKeyXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableQueryByKey()) {
            return this;
        }
        for (QueryByKeyProc queryByKeyProc : queryByKeyProcs) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDto key = queryByKeyProc.getKey();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    queryByKeyProc.getMethodName(), key.getJavaType().getName().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGeneratorConfig.getInstance()
                        .getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + key.getColumnName() + " = #{" + key.getPropertyName()
                    + "}");
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add("</select>");
            sourceCodeLines.addAll(xmlLines);
            sourceCodeLines.add("");
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}