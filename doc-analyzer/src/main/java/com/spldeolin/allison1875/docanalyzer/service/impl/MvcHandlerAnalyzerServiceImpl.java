package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.dto.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class MvcHandlerAnalyzerServiceImpl implements MvcHandlerAnalyzerService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AnalyzeMvcHandlerRetval analyzeMvcHandler(ClassOrInterfaceDeclaration mvcControllerCoid,
            MvcHandlerDTO mvcHandler) {
        AnalyzeMvcHandlerRetval result = new AnalyzeMvcHandlerRetval();
        result.setCat(mvcHandler.getCat().trim());
        List<String> descriptionLines = analyzeMethodDesc(mvcHandler);
        if (descriptionLines.stream().allMatch(StringUtils::isBlank)) {
            descriptionLines = Lists.newArrayList(
                    String.format("未指定注释（%s.%s）", mvcControllerCoid.getName(), mvcHandler.getMd().getName()));
        }
        result.setDescriptionLines(descriptionLines);
        result.setDeprecatedDescription(analyzeDeprecatedDescription(mvcControllerCoid, mvcHandler.getMd()));
        result.setSinceVersion(analyzeSinceVersion(mvcControllerCoid, mvcHandler.getMd()));
        result.setAuthor(JavadocUtils.getAuthor(mvcHandler.getMd()));
        result.setSourceCode(MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandler.getMd()));
        return result;
    }

    protected String analyzeSinceVersion(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        List<String> lines = JavadocUtils.getTagDescriptionAsLines(handler, Type.SINCE, null);
        if (lines.isEmpty()) {
            lines = JavadocUtils.getTagDescriptionAsLines(controller, Type.SINCE, null);
        }
        if (lines.isEmpty()) {
            return null;
        }
        return Joiner.on(System.lineSeparator()).join(lines);
    }

    protected String analyzeDeprecatedDescription(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        List<String> lines = JavadocUtils.getTagDescriptionAsLines(handler, Type.DEPRECATED, null);
        if (lines.isEmpty()) {
            lines = JavadocUtils.getTagDescriptionAsLines(controller, Type.DEPRECATED, null);
        }
        if (lines.isEmpty()) {
            return null;
        }
        return Joiner.on(System.lineSeparator()).join(lines);
    }

    protected List<String> analyzeMethodDesc(MvcHandlerDTO mvcHandler) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getDescriptionAsLines(mvcHandler.getMd());
    }

}