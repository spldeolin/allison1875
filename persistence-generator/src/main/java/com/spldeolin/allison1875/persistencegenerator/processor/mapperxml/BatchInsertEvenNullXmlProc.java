package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import static com.spldeolin.allison1875.base.constant.BaseConstant.SINGLE_INDENT;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchInsertEvenNullProc;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
public class BatchInsertEvenNullXmlProc extends XmlProc {

    private final PersistenceDto persistence;

    private final BatchInsertEvenNullProc batchInsertEvenNullProc;

    private Collection<String> sourceCodeLines;

    public BatchInsertEvenNullXmlProc(PersistenceDto persistence, BatchInsertEvenNullProc batchInsertEvenNullProc) {
        this.persistence = persistence;
        this.batchInsertEvenNullProc = batchInsertEvenNullProc;
    }

    public BatchInsertEvenNullXmlProc process() {
        if (PersistenceGeneratorConfig.getInstance().getDisableBatchInsertEvenNull()) {
            return this;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", batchInsertEvenNullProc.getMethodName()));
        xmlLines.add(SINGLE_INDENT + "<!-- @formatter:off -->");
        xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO %s", persistence.getTableName()));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "(<include refid=\"all\"/>)");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        xmlLines
                .add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "(");
        for (PropertyDto property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "#{one." + property.getPropertyName() + "},");
        }
        xmlLines
                .set(xmlLines.size() - 1, StringUtils.removeLast(Iterables.getLast(xmlLines), ","));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + ")");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add(SINGLE_INDENT + "<!-- @formatter:on -->");
        xmlLines.add("</insert>");
        this.sourceCodeLines = xmlLines;
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}