package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.CustomValidAnalyzerService;

/**
 * @author Deolin 2020-06-18
 */
@Singleton
public class CustomValidAnalyzerServiceImpl implements CustomValidAnalyzerService {

    @Override
    public List<ValidatorDto> analyzeCustomValid(AnnotatedElement annotatedElement) {
        return Lists.newArrayList();
    }

}
