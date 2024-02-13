package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.TransformMethodCallServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformMethodCallServiceImpl.class)
public interface TransformMethodCallService {

    String methodCallExpr(DesignMetaDto designMeta, ChainAnalysisDto chainAnalysis,
            GenerateParamRetval paramGeneration);

    List<Statement> argumentBuildStmts(ChainAnalysisDto chainAnalysis, GenerateParamRetval paramGeneration);

    List<Statement> mapOrMultimapBuildStmts(DesignMetaDto designMeta, ChainAnalysisDto chainAnalysis,
            GenerateReturnTypeRetval resultGeneration);

}