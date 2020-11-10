package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdProc;

/**
 * 根据ID更新数据，忽略值为null的属性
 *
 * @author Deolin 2020-07-19
 */
public class UpdateByIdXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private final UpdateByIdProc updateByIdProc;

    private Collection<String> sourceCodeLines;

    public UpdateByIdXmlProc(PersistenceDto persistence, String entityName, UpdateByIdProc updateByPkProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.updateByIdProc = updateByPkProc;
    }

    public UpdateByIdXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableUpdateById()) {
            return this;
        }
        if (persistence.getIdProperties().size() > 0) {
            List<String> xmlLines = Lists.newArrayList();
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", updateByIdProc.getMethodName(),
                    entityName));
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE " + persistence.getTableName());
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<set>");
            for (PropertyDto nonId : persistence.getNonIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + String
                        .format("<if test=\"%s!=null\"> %s = #{%s}, </if>", nonId.getPropertyName(),
                                nonId.getColumnName(), nonId.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</set>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE TRUE");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + PersistenceGeneratorConfig.getInstance()
                        .getNotDeletedSql());
            }
            for (PropertyDto id : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + String
                        .format("  AND %s = #{%s}", id.getColumnName(), id.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</update>");
            sourceCodeLines = xmlLines;
            sourceCodeLines.add("");
        }
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}