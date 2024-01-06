package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.ast.Annotations;
import com.spldeolin.allison1875.common.util.ast.Authors;
import com.spldeolin.allison1875.common.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.AccessDescriptionService;
import com.spldeolin.allison1875.docanalyzer.service.MoreHandlerAnalysisService;
import com.spldeolin.allison1875.docanalyzer.service.SimplyAnalyzeService;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class SimplyAnalyzeServiceImpl implements SimplyAnalyzeService {

    @Inject
    private AccessDescriptionService accessDescriptionService;

    @Inject
    private MoreHandlerAnalysisService moreHandlerAnalysisService;

    @Override
    public void process(ClassOrInterfaceDeclaration controller, HandlerFullDto handler, EndpointDto endpoint) {
        endpoint.setCat(handler.getCat().trim());
        endpoint.setHandlerSimpleName(controller.getName() + "_" + handler.getMd().getName());
        endpoint.setDescriptionLines(Lists.newArrayList(accessDescriptionService.accessMethod(handler)));
        endpoint.setIsDeprecated(isDeprecated(controller, handler.getMd()));
        endpoint.setAuthor(Authors.getAuthor(handler.getMd()));
        endpoint.setSourceCode(MethodQualifiers.getTypeQualifierWithMethodName(handler.getMd()));
        endpoint.setMore(moreHandlerAnalysisService.moreAnalysisFromMethod(handler));
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnotationPresent(handler, Deprecated.class) || Annotations.isAnnotationPresent(controller,
                Deprecated.class);
    }

}