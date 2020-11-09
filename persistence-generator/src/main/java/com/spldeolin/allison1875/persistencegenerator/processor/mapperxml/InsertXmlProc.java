package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.InsertProc;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
public class InsertXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final String entityName;

    private final InsertProc insertProc;

    private Collection<String> sourceCodeLines;

    public InsertXmlProc(PersistenceDto persistence, String entityName, InsertProc insertProc) {
        this.persistence = persistence;
        this.entityName = entityName;
        this.insertProc = insertProc;
    }

    public InsertXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableInsert()) {
            return this;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\" parameterType=\"%s\">", insertProc.getMethodName(), entityName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO " + persistence.getTableName());
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String
                    .format("<if test=\"%s!=null\"> %s, </if>", property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String
                    .format("<if test=\"%s!=null\"> #{%s}, </if>", property.getPropertyName(),
                            property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
        xmlLines.add("</insert>");

        sourceCodeLines = xmlLines;
        sourceCodeLines.add("");
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}