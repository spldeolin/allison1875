package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeEnumConstantRetval;
import com.spldeolin.allison1875.docanalyzer.service.EnumConstantAnalyzerService;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class EnumConstantAnalyzerServiceImpl implements EnumConstantAnalyzerService {

    @Override
    public boolean isSupport(Class<?> enumType) {
        return false;
    }

    @Override
    public AnalyzeEnumConstantRetval analyzeEnumConstant(Object enumConstant) {
        // what ever
        return null;
    }

}