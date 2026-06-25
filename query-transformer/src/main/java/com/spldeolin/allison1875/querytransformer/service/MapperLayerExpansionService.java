package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.DefaultMapperLayerExpansionServiceImpl;

/**
 * @author Deolin 2026-06-26
 */
@ImplementedBy(DefaultMapperLayerExpansionServiceImpl.class)
public interface MapperLayerExpansionService {

    ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis);

    List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin);

}
