package com.spldeolin.allison1875.docanalyzer.handle;

import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2021-04-08
 */
public interface MoreHandlerAnalysisHandle {

    Object moreAnalysisFromMethod(HandlerFullDto handler);

    String moreToString(Object dto);

}
