package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

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
        List<String> sourceCodeLines = Lists.newArrayList();
        sourceCodeLines.add(String.format("<insert id=\"%s\">", batchInsertEvenNullProc.getMethodName()));
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO %s", persistence.getTableName()));
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "(<include refid=\"all\"/>)");
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        sourceCodeLines
                .add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">");
        sourceCodeLines.add(BaseConstant.DOUBLE_INDENT + "(");
        for (PropertyDto property : persistence.getProperties()) {
            sourceCodeLines.add(BaseConstant.DOUBLE_INDENT + "#{one." + property.getPropertyName() + "},");
        }
        sourceCodeLines
                .set(sourceCodeLines.size() - 1, StringUtils.removeLast(Iterables.getLast(sourceCodeLines), ","));
        sourceCodeLines.add(BaseConstant.DOUBLE_INDENT + ")");
        sourceCodeLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        sourceCodeLines.add("</insert>");
        this.sourceCodeLines = sourceCodeLines;
        return this;
    }

    public Collection<String> getSourceCodeLines() {
        return this.sourceCodeLines;
    }

}