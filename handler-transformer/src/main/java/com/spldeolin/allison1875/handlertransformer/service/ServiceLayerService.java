package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.dto.AddMethodToServiceArgs;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplArgs;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceLayerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ServiceLayerServiceImpl.class)
public interface ServiceLayerService {

    GenerateServiceMethodRetval generateServiceMethod(InitDecAnalysisDTO initDecAnalysisDTO, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType);

    String addMethodToService(AddMethodToServiceArgs args);

    GenerateServiceAndImplRetval generateServiceAndImpl(GenerateServiceAndImplArgs args);

}