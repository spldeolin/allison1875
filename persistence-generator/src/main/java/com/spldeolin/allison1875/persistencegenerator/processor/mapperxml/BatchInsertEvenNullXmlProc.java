package com.spldeolin.allison1875.persistencegenerator.processor.mapperxml;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * 插入
 *
 * @author Deolin 2020-07-19
 */
@Singleton
public class BatchInsertEvenNullXmlProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<String> process(PersistenceDto persistence, String methodName) {
        if (persistenceGeneratorConfig.getDisableBatchInsertEvenNull()) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_OFF_MARKER);
        xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO %s", persistence.getTableName()));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "( <include refid=\"all\"/> )");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">");
        xmlLines.add(BaseConstant.SINGLE_INDENT + Constant.FORMATTER_ON_MARKER);
        xmlLines.add(BaseConstant.TREBLE_INDENT + "( " + persistence.getProperties().stream()
                .map(p -> "#{one." + p.getPropertyName() + "}").collect(Collectors.joining(", ")) + " )");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</foreach>");
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

}