package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.GenerateMethodSignatureServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateMethodSignatureServiceImpl.class)
public interface GenerateMethodSignatureService {

    CompilationUnit process(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation);

}