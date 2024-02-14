package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.MemberAdderService;

/**
 * @author Deolin 2024-01-23
 */
@Singleton
public class MemberAdderServiceImpl implements MemberAdderService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public void addMethodToCoid(MethodDeclaration method, ClassOrInterfaceDeclaration coid) {
        coid.addMember(method);
    }

    @Override
    public List<FileFlush> addMethodToXml(List<String> xmlMethodCodeLines, TrackCoidDto trackCoid) {
        List<FileFlush> flushes = Lists.newArrayList();
        for (File mapperXml : trackCoid.getMapperXmls()) {
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

    @Override
    public void ensureAuwired(ClassOrInterfaceDeclaration toBeAutowired, ClassOrInterfaceDeclaration coid) {
        String qualifier = toBeAutowired.getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(toBeAutowired));

        String fieldVarName = MoreStringUtils.lowerFirstLetter(toBeAutowired.getNameAsString());
        if (!coid.getFieldByName(fieldVarName).isPresent()) {
            NodeList<BodyDeclaration<?>> members = coid.getMembers();
            int lastIndexOfFieldDeclaration = IntStream.range(0, members.size())
                    .filter(i -> members.get(i) instanceof FieldDeclaration).reduce((first, second) -> second)
                    .orElse(-1);
            FieldDeclaration field = new ClassOrInterfaceDeclaration().addField(qualifier, fieldVarName,
                    Keyword.PRIVATE);
            field.addAnnotation(annotationExprService.springAutowired());
            members.add(lastIndexOfFieldDeclaration + 1, field);
        }
    }

}