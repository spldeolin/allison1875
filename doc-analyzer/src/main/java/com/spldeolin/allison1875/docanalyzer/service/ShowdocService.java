package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.ShowdocServiceImpl;

/**
 * @author Deolin 2025-03-15
 */
@ImplementedBy(ShowdocServiceImpl.class)
public interface ShowdocService {

    void flushToShowdoc(List<EndpointDTO> endpoints);

}
