package com.spldeolin.allison1875.startransformer.service;

import java.util.Set;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.AnalyzeChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(AnalyzeChainServiceImpl.class)
public interface AnalyzeChainService {

    ChainAnalysisDto process(MethodCallExpr starChain, AstForest astForest, Set<String> wholeDtoNames)
            throws IllegalChainException;

    String buildWholeDtoNameFromEntityName(String entityName);

}