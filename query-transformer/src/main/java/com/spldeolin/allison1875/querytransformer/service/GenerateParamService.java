package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.GenerateParamServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateParamServiceImpl.class)
public interface GenerateParamService {

    ParamGenerationDto generate(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest);

}