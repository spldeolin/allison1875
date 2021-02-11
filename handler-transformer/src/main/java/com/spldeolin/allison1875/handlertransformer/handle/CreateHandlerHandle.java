package com.spldeolin.allison1875.handlertransformer.handle;

import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateHandlerHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-01-10
 */
public interface CreateHandlerHandle {

    CreateHandlerHandleResult createHandler(FirstLineDto firstLineDto, String serviceParamType,
            String serviceResultType, SingleMethodServiceCuBuilder serviceCuBuilder);

}