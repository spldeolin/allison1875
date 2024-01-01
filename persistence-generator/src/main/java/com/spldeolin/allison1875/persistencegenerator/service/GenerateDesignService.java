package com.spldeolin.allison1875.persistencegenerator.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.service.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.GenerateDesignServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(GenerateDesignServiceImpl.class)
public interface GenerateDesignService {

    CompilationUnit process(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper, AstForest astForest);

}