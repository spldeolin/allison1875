package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;

/**
 * @author Deolin 2021-03-05
 */
@Data
public class ServiceGeneration {

    private String serviceVarName;

    private ClassOrInterfaceDeclaration service;

    private String serviceQualifier;

    private String methodName;

}