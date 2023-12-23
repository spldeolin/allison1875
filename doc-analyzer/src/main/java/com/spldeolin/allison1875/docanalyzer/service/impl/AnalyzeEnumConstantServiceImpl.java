package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.service.AnalyzeEnumConstantService;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class AnalyzeEnumConstantServiceImpl implements AnalyzeEnumConstantService {

    @Override
    public boolean supportEnumType(Class<?> enumType) {
        return false;
    }

    @Override
    public EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant) {
        // what ever
        return null;
    }

}