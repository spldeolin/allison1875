package com.spldeolin.allison1875.handlertransformer.handle;

import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;

/**
 * @author Deolin 2021-01-10
 */
public interface CreateServiceMethodHandle {

    CreateServiceMethodHandleResult createMethodImpl(FirstLineDto firstLineDto, String paramType, String resultType);

}