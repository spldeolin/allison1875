package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
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

    final List<ClassOrInterfaceDeclaration> serviceImpls = Lists.newArrayList();

    String serviceQualifier;

    String methodName;

}