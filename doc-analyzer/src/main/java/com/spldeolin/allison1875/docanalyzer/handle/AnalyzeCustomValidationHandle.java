package com.spldeolin.allison1875.docanalyzer.handle;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import javax.inject.Singleton;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;

/**
 * @author Deolin 2020-06-18
 */
@Singleton
public class AnalyzeCustomValidationHandle {

    public Collection<ValidatorDto> analyzeCustomValidation(AnnotatedElement annotatedElement) {
        return Lists.newArrayList();
    }

}
