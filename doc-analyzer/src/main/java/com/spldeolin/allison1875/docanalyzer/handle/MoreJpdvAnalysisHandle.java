package com.spldeolin.allison1875.docanalyzer.handle;

import java.lang.reflect.Field;

/**
 * jpdv额外解析
 *
 * @author Deolin 2020-12-02
 */
public interface MoreJpdvAnalysisHandle {

    Object moreAnalysisFromField(Field field);

    String moreJpdvToString(Object dto);

}