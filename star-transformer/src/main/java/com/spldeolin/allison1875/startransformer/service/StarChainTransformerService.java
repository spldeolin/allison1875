package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.startransformer.javabean.TransformStarChainArgs;
import com.spldeolin.allison1875.startransformer.service.impl.TransformStarChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformStarChainServiceImpl.class)
public interface StarChainTransformerService {

    void transformStarChain(TransformStarChainArgs args);

}