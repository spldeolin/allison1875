package com.spldeolin.allison1875.querytransformer.service;

import java.util.Optional;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.GenerateMethodIntoMapperServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateMethodIntoMapperServiceImpl.class)
public interface GenerateMethodIntoMapperService {

    Optional<FileFlush> generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration);

}