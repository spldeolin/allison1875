package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.service.impl.OffsetMethodNameServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(OffsetMethodNameServiceImpl.class)
public interface OffsetMethodNameService {

    CompilationUnit useOffsetMethod(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            ClassOrInterfaceDeclaration design);

}