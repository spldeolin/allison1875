package com.spldeolin.allison1875.startransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.AnalyzeStarChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(AnalyzeStarChainServiceImpl.class)
public interface AnalyzeStarChainService {

    StarAnalysisDto analyze(MethodCallExpr starChain, AstForest astForest) throws IllegalChainException;

    String buildWholeDtoNameFromEntityName(String entityName);

}