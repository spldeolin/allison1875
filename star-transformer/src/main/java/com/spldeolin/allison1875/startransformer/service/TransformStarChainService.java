package com.spldeolin.allison1875.startransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.TransformStarChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformStarChainServiceImpl.class)
public interface TransformStarChainService {

    void transformStarChain(BlockStmt block, StarAnalysisDto analysis, MethodCallExpr starChain,
            JavabeanGeneration javabeanGeneration);

}