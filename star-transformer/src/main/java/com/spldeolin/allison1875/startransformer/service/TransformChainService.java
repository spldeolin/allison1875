package com.spldeolin.allison1875.startransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.TransformChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformChainServiceImpl.class)
public interface TransformChainService {

    void transformAndReplaceStar(BlockStmt block, ChainAnalysisDto analysis, MethodCallExpr starChain,
            JavabeanGeneration javabeanGeneration);

}