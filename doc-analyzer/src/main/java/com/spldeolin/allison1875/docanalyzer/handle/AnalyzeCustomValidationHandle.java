package com.spldeolin.allison1875.docanalyzer.handle;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;

/**
 * 解析自定义校验注解策略
 *
 * @author Deolin 2020-06-18
 */
public interface AnalyzeCustomValidationHandle {

    Collection<ValidatorDto> analyzeCustomValidation(AnnotatedElement annotatedElement);

}
