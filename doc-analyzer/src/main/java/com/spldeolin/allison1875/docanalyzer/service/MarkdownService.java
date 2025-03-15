package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.CategorizedMarkdownDTO;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.MarkdownServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MarkdownServiceImpl.class)
public interface MarkdownService {

    List<CategorizedMarkdownDTO> categorizeMarkdowns(List<EndpointDTO> endpoints);

    void flushToMarkdown(List<EndpointDTO> endpoints);

}
