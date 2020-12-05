package com.spldeolin.allison1875.docanalyzer.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2020-12-04
 */
public class SimplyAnalyzeProc {

    public void process(ClassOrInterfaceDeclaration controller, HandlerFullDto handler, EndpointDto endpoint) {
        endpoint.setCat(handler.getCat());
        endpoint.setHandlerSimpleName(controller.getName() + "_" + handler.getMd().getName());
        endpoint.setDescriptionLines(JavadocDescriptions.getAsLines(handler.getMd()));
        endpoint.setIsDeprecated(isDeprecated(controller, handler.getMd()));
        endpoint.setAuthor(Authors.getAuthor(handler.getMd()));
        endpoint.setSourceCode(MethodQualifiers.getTypeQualifierWithMethodName(handler.getMd()));
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnotationPresent(handler, Deprecated.class) || Annotations
                .isAnnotationPresent(controller, Deprecated.class);
    }

}