package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.service.impl.AnalyzeChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(AnalyzeChainServiceImpl.class)
public interface AnalyzeChainService {

    ChainAnalysisDto analyze(MethodCallExpr chain, ClassOrInterfaceDeclaration design, DesignMeta designMeta)
            throws IllegalChainException;

}