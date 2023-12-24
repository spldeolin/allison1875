package com.spldeolin.allison1875.persistencegenerator.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.DeleteAllison1875MethodServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(DeleteAllison1875MethodServiceImpl.class)
public interface DeleteAllison1875MethodService {

    void process(ClassOrInterfaceDeclaration mapper);

}