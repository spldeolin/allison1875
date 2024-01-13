package com.spldeolin.allison1875.persistencegenerator.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.service.impl.DeleteAllison1875MethodServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(DeleteAllison1875MethodServiceImpl.class)
public interface DeleteAllison1875MethodService {

    void deleteMethod(ClassOrInterfaceDeclaration mapper);

}