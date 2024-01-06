package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.GenerateMethodXmlServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateMethodXmlServiceImpl.class)
public interface GenerateMethodXmlService {

    List<FileFlush> generate(AstForest astForest, DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParamGenerationDto paramGeneration, ResultGenerationDto resultGeneration);

}