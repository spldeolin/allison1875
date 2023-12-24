package com.spldeolin.allison1875.handlertransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.CreateServiceMethodServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(CreateServiceMethodServiceImpl.class)
public interface CreateServiceMethodService {

    CreateServiceMethodHandleResult createMethodImpl(FirstLineDto firstLineDto, String paramType, String resultType);

}