package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class BatchInsertXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchInsert()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "INSERT INTO " + persistence.getTableName());
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String
                    .format("<if test=\"one.%s!=null\"> %s, </if>", property.getPropertyName(),
                            property.getColumnName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"VALUE (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String
                    .format("<if test=\"one.%s!=null\"> #{one.%s}, </if>", property.getPropertyName(),
                            property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>;");
        xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

}