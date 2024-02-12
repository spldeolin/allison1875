package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MvcHandlerMoreAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MvcHandlerMoreAnalyzerServiceImpl.class)
public interface MvcHandlerMoreInfoAnalyzerService {

    Object moreAnalyzeMvcHandler(MvcHandlerDto mvcHandler);

    String formatMoreInfo(Object moreInfo);

}