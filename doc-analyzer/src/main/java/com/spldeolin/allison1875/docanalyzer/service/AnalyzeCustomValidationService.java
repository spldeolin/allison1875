package com.spldeolin.allison1875.docanalyzer.service;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.AnalyzeCustomValidationServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(AnalyzeCustomValidationServiceImpl.class)
public interface AnalyzeCustomValidationService {

    List<ValidatorDto> analyzeCustomValidation(AnnotatedElement annotatedElement);

}
