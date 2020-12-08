package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;

/**
 * 根据主键更新，即便属性的值为null，也更新为null
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdEvenNullXmlProc {

    public Collection<String> process(PersistenceDto persistence, String entityName, String methodName) {
        if (PersistenceGenerator.CONFIG.get().getDisableUpdateByIdEvenNull()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() > 0) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SET");
            for (PropertyDto nonId : persistence.getNonIdProperties()) {
                xmlLines.add(
                        BaseConstant.DOUBLE_INDENT + nonId.getColumnName() + " = #{" + nonId.getPropertyName() + "},");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(
                        BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGenerator.CONFIG.get().getNotDeletedSql());
            }
            for (PropertyDto idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + idProperty.getColumnName() + " = #{" + idProperty
                        .getPropertyName() + "}");
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add("</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

}