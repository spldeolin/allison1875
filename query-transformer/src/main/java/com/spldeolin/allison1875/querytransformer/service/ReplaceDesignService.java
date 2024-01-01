package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.ReplaceDesignServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(ReplaceDesignServiceImpl.class)
public interface ReplaceDesignService {

    void process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis, ParamGenerationDto paramGeneration,
            ResultGenerationDto resultGeneration);

}