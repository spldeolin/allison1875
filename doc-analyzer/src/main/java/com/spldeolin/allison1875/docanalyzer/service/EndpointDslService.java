package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.EndpointDslServiceImpl;

/**
 * @author Deolin 2025-03-17
 */
@ImplementedBy(EndpointDslServiceImpl.class)
public interface EndpointDslService {

    void flushToEndpointDsl(List<EndpointDTO> endpoints);

}
