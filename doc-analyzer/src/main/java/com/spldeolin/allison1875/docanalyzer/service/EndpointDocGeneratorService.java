package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.EndpointDocGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(EndpointDocGeneratorServiceImpl.class)
public interface EndpointDocGeneratorService {

    String generateDocForYApi(EndpointDto endpoint);

}