package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.Field;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.service.MoreJpdvAnalysisService;

/**
 * @author Deolin 2020-12-02
 */
@Singleton
public class MoreJpdvAnalysisServiceImpl implements MoreJpdvAnalysisService {

    @Override
    public Object moreAnalysisFromField(Field field) {
        return null;
    }

    @Override
    public String moreJpdvToString(Object dto, DocAnalyzerConfig docAnalyzerConfig) {
        return null;
    }

}