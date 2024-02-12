package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.DescAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerMoreInfoAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.util.AnnotationUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class MvcHandlerAnalyzerServiceImpl implements MvcHandlerAnalyzerService {

    @Inject
    private DescAnalyzerService descAnalyzerService;

    @Inject
    private MvcHandlerMoreInfoAnalyzerService mvcHandlerMoreInfoAnalyzerService;

    @Override
    public AnalyzeMvcHandlerRetval analyzeMvcHandler(ClassOrInterfaceDeclaration mvcControllerCoid,
            MvcHandlerDto mvcHandler) {
        AnalyzeMvcHandlerRetval result = new AnalyzeMvcHandlerRetval();
        result.setCat(mvcHandler.getCat().trim());
        result.setHandlerSimpleName(mvcControllerCoid.getName() + "_" + mvcHandler.getMethodDec().getName());
        result.setDescriptionLines(Lists.newArrayList(descAnalyzerService.ananlyzeMethodDesc(mvcHandler)));
        result.setIsDeprecated(isDeprecated(mvcControllerCoid, mvcHandler.getMethodDec()));
        result.setAuthor(JavadocUtils.getAuthor(mvcHandler.getMethodDec()));
        result.setSourceCode(MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandler.getMethodDec()));
        result.setMoreInfo(mvcHandlerMoreInfoAnalyzerService.moreAnalyzeMvcHandler(mvcHandler));
        return result;
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return AnnotationUtils.isAnnotationPresent(handler, Deprecated.class) || AnnotationUtils.isAnnotationPresent(
                controller, Deprecated.class);
    }

}