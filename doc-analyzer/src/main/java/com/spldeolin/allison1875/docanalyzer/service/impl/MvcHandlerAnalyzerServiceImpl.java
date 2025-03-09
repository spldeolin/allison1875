package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.CommonConfig;
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

    @Inject
    private CommonConfig commonConfig;

    @Override
    public AnalyzeMvcHandlerRetval analyzeMvcHandler(ClassOrInterfaceDeclaration mvcControllerCoid,
            MvcHandlerDTO mvcHandler) {
        AnalyzeMvcHandlerRetval result = new AnalyzeMvcHandlerRetval();
        result.setDirectCategory(analyzeDirectCategory(mvcControllerCoid));
        result.setHierarchicalCategories(analyzeHierarchicalCategories(mvcControllerCoid));
        List<String> descriptionLines = analyzeMethodDesc(mvcHandler);
        if (descriptionLines.stream().allMatch(StringUtils::isBlank)) {
            descriptionLines = Lists.newArrayList(
                    String.format("%s.%s", mvcControllerCoid.getName(), mvcHandler.getMd().getName()));
        }
        result.setDescriptionLines(descriptionLines);
        result.setDeprecatedDescription(analyzeDeprecatedDescription(mvcControllerCoid, mvcHandler.getMd()));
        result.setSinceVersion(analyzeSinceVersion(mvcControllerCoid, mvcHandler.getMd()));
        result.setAuthor(JavadocUtils.getAuthor(mvcHandler.getMd()));
        result.setSourceCode(MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandler.getMd()));
        return result;
    }

    protected String analyzeDirectCategory(ClassOrInterfaceDeclaration mvcController) {
        String retval = Iterables.getFirst(JavadocUtils.getDescriptionAsLines(mvcController), "");
        if (StringUtils.isBlank(retval)) {
            retval = mvcController.getNameAsString();
        }
        return retval;
    }

    protected List<String> analyzeHierarchicalCategories(ClassOrInterfaceDeclaration mvcController) {
        List<String> retval = Lists.newArrayList();
        mvcController.findCompilationUnit().flatMap(CompilationUnit::getPackageDeclaration).ifPresent(pd -> {
            JavadocUtils.getDescriptionFirstLineInPackageInfos(pd, AstForestContext.get()).forEach((pkg, desc) -> {
                if (StringUtils.isNotBlank(desc)) {
                    retval.add(desc);
                } else {
                    pkg = StringUtils.removeStart(pkg, commonConfig.getBasePackage());
                    pkg = StringUtils.removeStart(pkg, ".");
                    retval.add(pkg);
                }
            });
        });
        return retval;
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