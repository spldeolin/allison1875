package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.GenerateResultServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateResultServiceImpl.class)
public interface GenerateResultService {

    ResultGenerationDto generate(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest);

}