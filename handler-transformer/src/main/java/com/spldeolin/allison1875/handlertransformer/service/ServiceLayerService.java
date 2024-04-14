package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.AddMethodToServiceArgs;
import com.spldeolin.allison1875.handlertransformer.javabean.AddMethodToServiceRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplArgs;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceLayerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ServiceLayerServiceImpl.class)
public interface ServiceLayerService {

    MethodDeclaration generateServiceMethod(InitDecAnalysisDto initDecAnalysisDto, String paramType, String resultType);

    AddMethodToServiceRetval addMethodToService(AddMethodToServiceArgs args);

    GenerateServiceAndImplRetval generateServiceAndImpl(GenerateServiceAndImplArgs args);

}