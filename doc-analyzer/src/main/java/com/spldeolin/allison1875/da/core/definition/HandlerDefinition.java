package com.spldeolin.allison1875.da.core.definition;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.types.ResolvedType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-02
 */
@Data
@Accessors(fluent = true)
public class HandlerDefinition {

    private ClassOrInterfaceDeclaration controller;

    private String shortestQualifiedSignature;

    private MethodDeclaration handler;

    private Collection<Parameter> pathVariables;

    private Collection<Parameter> requestParams;

    private ResolvedType requestBodyResolvedType;

    private ResolvedType responseBodyResolvedType;

    @Override
    public String toString() {
        return "Handler=[" + shortestQualifiedSignature + ']';
    }

}