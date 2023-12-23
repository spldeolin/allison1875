package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MarkdownOutputServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MarkdownOutputServiceImpl.class)
public interface MarkdownOutputService {

    void process(Collection<EndpointDto> endpoints) throws Exception;

}
