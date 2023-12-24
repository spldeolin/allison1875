package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ServiceServiceImpl.class)
public interface ServiceService {

    SingleMethodServiceCuBuilder generateServiceWithImpl(CompilationUnit cu, FirstLineDto firstLineDto,
            ReqDtoRespDtoInfo reqDtoRespDtoInfo);

}