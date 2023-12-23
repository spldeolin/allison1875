package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.EndpointToStringServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(EndpointToStringServiceImpl.class)
public interface EndpointToStringService {

    String toString(EndpointDto dto);

}