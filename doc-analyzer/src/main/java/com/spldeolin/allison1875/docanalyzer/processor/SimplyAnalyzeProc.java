package com.spldeolin.allison1875.docanalyzer.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.handle.AccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreHandlerAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class SimplyAnalyzeProc {

    @Inject
    private AccessDescriptionHandle accessDescriptionHandle;

    @Inject
    private MoreHandlerAnalysisHandle moreHandlerAnalysisHandle;

    public void process(ClassOrInterfaceDeclaration controller, HandlerFullDto handler, EndpointDto endpoint) {
        endpoint.setCat(handler.getCat().trim());
        endpoint.setHandlerSimpleName(controller.getName() + "_" + handler.getMd().getName());
        endpoint.setDescriptionLines(accessDescriptionHandle.accessMethod(handler));
        endpoint.setIsDeprecated(isDeprecated(controller, handler.getMd()));
        endpoint.setAuthor(Authors.getAuthor(handler.getMd()));
        endpoint.setSourceCode(MethodQualifiers.getTypeQualifierWithMethodName(handler.getMd()));
        endpoint.setMore(moreHandlerAnalysisHandle.moreAnalysisFromMethod(handler));
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnotationPresent(handler, Deprecated.class) || Annotations.isAnnotationPresent(controller,
                Deprecated.class);
    }

}