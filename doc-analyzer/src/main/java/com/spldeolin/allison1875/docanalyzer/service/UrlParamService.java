package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.dto.PathParamDTO;
import com.spldeolin.allison1875.docanalyzer.dto.QueryParamDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.UrlParamServiceImpl;

/**
 * @author Deolin 2025-01-24
 */
@ImplementedBy(UrlParamServiceImpl.class)
public interface UrlParamService {

    List<QueryParamDTO> analyzeQueryParams(MvcHandlerDTO mvcHandler);

    List<PathParamDTO> analyzePathParams(MvcHandlerDTO mvcHandler);

}
