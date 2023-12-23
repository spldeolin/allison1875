package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MoreHandlerAnalysisServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MoreHandlerAnalysisServiceImpl.class)
public interface MoreHandlerAnalysisService {

    Object moreAnalysisFromMethod(HandlerFullDto handler);

    String moreToString(Object dto);

}