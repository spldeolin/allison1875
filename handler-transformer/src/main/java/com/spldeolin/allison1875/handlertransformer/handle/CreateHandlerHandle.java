package com.spldeolin.allison1875.handlertransformer.handle;

import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateHandlerHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;

/**
 * @author Deolin 2021-01-10
 */
public interface CreateHandlerHandle {

    CreateHandlerHandleResult createHandler(FirstLineDto firstLineDto, String serviceParamType,
            String serviceResultType, ServiceGeneration serviceGeneration);

}