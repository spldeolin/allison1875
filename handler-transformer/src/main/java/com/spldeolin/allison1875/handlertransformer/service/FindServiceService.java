package com.spldeolin.allison1875.handlertransformer.service;

import java.util.Map;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.FindServiceServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(FindServiceServiceImpl.class)
public interface FindServiceService {

    ServicePairDto findPresent(AstForest astForest, String presentServiceQualifier,
            Map<String, ServicePairDto> qualifier2Pair);

    ServicePairDto findGenerated(String serviceName, Map<String, ServicePairDto> name2Pair, AstForest astForest);

}