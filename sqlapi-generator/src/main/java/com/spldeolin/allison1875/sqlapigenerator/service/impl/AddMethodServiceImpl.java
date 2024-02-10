package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.service.AddMethodService;

/**
 * @author Deolin 2024-01-23
 */
@Singleton
public class AddMethodServiceImpl implements AddMethodService {

    @Override
    public void addMethodToCoid(MethodDeclaration method, ClassOrInterfaceDeclaration coid) {
        coid.addMember(method);
    }

    @Override
    public List<FileFlush> addMethodToXml(List<String> xmlMethodCodeLines, CoidsOnTrackDto coidsOnTrackDto) {
        List<FileFlush> flushes = Lists.newArrayList();
        for (File mapperXml : coidsOnTrackDto.getMapperXmls()) {
            List<String> lines;
            try {
                lines = FileUtils.readLines(mapperXml, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            List<String> newLines = Lists.newArrayList();
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(xmlMethodCodeLines);
                    for (String xmlLine : xmlMethodCodeLines) {
                        if (StringUtils.isNotBlank(xmlLine)) {
                            newLines.add(SINGLE_INDENT + xmlLine);
                        }
                    }
                    newLines.add("");
                }
            }
            Collections.reverse(newLines);
            flushes.add(FileFlush.build(mapperXml, Joiner.on('\n').join(newLines)));
        }
        return flushes;
    }

}