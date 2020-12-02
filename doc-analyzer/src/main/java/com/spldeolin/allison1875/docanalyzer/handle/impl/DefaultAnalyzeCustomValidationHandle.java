package com.spldeolin.allison1875.docanalyzer.handle.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeCustomValidationHandle;

/**
 * @author Deolin 2020-06-18
 */
public class DefaultAnalyzeCustomValidationHandle implements AnalyzeCustomValidationHandle {

    @Override
    public Collection<ValidatorDto> analyzeCustomValidation(AnnotatedElement annotatedElement) {
        return Lists.newArrayList();
    }

}
