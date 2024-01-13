package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.CopyEndpointServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(CopyEndpointServiceImpl.class)
public interface CopyEndpointService {

    Collection<EndpointDto> copy(EndpointDto endpoint, RequestMappingFullDto requestMappingFullDto);

}