package com.spldeolin.allison1875.handlertransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.service.impl.CreateHandlerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(CreateHandlerServiceImpl.class)
public interface CreateHandlerService {

    HandlerCreation createHandler(FirstLineDto firstLineDto, String serviceParamType, String serviceResultType,
            ServiceGeneration serviceGeneration);

}