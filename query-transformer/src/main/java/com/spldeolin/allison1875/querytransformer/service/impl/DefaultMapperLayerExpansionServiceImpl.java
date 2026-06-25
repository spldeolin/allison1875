package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Collections;
import java.util.List;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-26
 */
@Singleton
@Slf4j
public class DefaultMapperLayerExpansionServiceImpl implements MapperLayerExpansionService {

    @Override
    public ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis) {
        return new ExpandParamRetval();
    }

    @Override
    public List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin) {
        return Collections.emptyList();
    }

}
