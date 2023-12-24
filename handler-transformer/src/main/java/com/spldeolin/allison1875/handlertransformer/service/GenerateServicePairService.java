package com.spldeolin.allison1875.handlertransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.service.impl.GenerateServicePairServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(GenerateServicePairServiceImpl.class)
public interface GenerateServicePairService {

    ServiceGeneration generateService(GenerateServiceParam param);

}