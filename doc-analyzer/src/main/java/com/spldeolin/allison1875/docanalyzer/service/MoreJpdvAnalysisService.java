package com.spldeolin.allison1875.docanalyzer.service;

import java.lang.reflect.Field;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.service.impl.MoreJpdvAnalysisServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MoreJpdvAnalysisServiceImpl.class)
public interface MoreJpdvAnalysisService {

    Object moreAnalysisFromField(Field field);

    String moreJpdvToString(Object dto, DocAnalyzerConfig docAnalyzerConfig);

}