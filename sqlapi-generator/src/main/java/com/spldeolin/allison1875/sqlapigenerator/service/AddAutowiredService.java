package com.spldeolin.allison1875.sqlapigenerator.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.AddAutowiredServiceImpl;

/**
 * @author Deolin 2024-01-22
 */
@ImplementedBy(AddAutowiredServiceImpl.class)
public interface AddAutowiredService {

    void ensureAuwired(ClassOrInterfaceDeclaration toBeAutowired, ClassOrInterfaceDeclaration coid);

}