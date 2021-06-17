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
import jodd.util.StringUtil;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class BatchUpdateXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchUpdate()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "UPDATE " + persistence.getTableName());
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "SET");
        for (PropertyDto nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String
                    .format("<if test=\"one.%s!=null\"> %s = #{one.%s}, </if>", nonId.getPropertyName(),
                            nonId.getColumnName(), nonId.getPropertyName()));
        }
        // 删除最后一个语句中，最后的逗号
        if (xmlLines.size() > 0) {
            int last = xmlLines.size() - 1;
            xmlLines.set(last, StringUtil.cutSuffix(xmlLines.get(last), ", </if>") + " </if>");
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "WHERE TRUE");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + persistenceGeneratorConfig.getNotDeletedSql());
        }
        for (PropertyDto idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + idProperty.getColumnName() + " = #{one." + idProperty
                    .getPropertyName() + "}");
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

}