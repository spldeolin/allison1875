package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceGeneration {

     String serviceVarName;

     ClassOrInterfaceDeclaration service;

     String serviceQualifier;

     String methodName;

}