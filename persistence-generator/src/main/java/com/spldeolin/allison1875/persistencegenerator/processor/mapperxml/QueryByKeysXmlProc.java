package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeysProc;

/**
 * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
 *
 * @author Deolin 2020-07-19
 */
public class QueryByKeysXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final Collection<QueryByKeysProc> queryByKeysProcs;

    private final Collection<String> sourceCodeLines = Lists.newArrayList();

    public QueryByKeysXmlProc(PersistenceDto persistence, Collection<QueryByKeysProc> queryByKeysProcs) {
        this.persistence = persistence;
        this.queryByKeysProcs = queryByKeysProcs;
    }

    public QueryByKeysXmlProc process() {
        if (PersistenceGenerator.CONFIG.get().getDisableQueryByKeys()) {
            return this;
        }
        for (QueryByKeysProc queryByKeyProc : queryByKeysProcs) {
            List<String> xmlLines = Lists.newArrayList();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    queryByKeyProc.getMethodName(),
                    queryByKeyProc.getKey().getJavaType().getName().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(
                        BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGenerator.CONFIG.get().getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + queryByKeyProc.getKey().getColumnName() + String
                    .format(" IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                            queryByKeyProc.getVarsName()));
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