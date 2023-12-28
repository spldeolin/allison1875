package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.MapOrMultimapBuiltDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.TransformMethodCallServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformMethodCallServiceImpl.class)
public interface TransformMethodCallService {

    String methodCallExpr(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation);

    List<Statement> argumentBuildStmts(ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation);

    MapOrMultimapBuiltDto mapOrMultimapBuildStmts(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ResultTransformationDto resultTransformation);

}