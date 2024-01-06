package com.spldeolin.allison1875.persistencegenerator.service;

import java.io.IOException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.service.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.FindOrCreateMapperServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(FindOrCreateMapperServiceImpl.class)
public interface FindOrCreateMapperService {

    ClassOrInterfaceDeclaration process(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            AstForest astForest) throws IOException;

}