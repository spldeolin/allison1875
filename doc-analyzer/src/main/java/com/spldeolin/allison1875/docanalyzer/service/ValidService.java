package com.spldeolin.allison1875.docanalyzer.service;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.ValidServiceImpl;

/**
 * 校验项
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ValidServiceImpl.class)
public interface ValidService {

    List<ValidatorDto> analyzeValid(AnnotatedElement annotatedElement);

}
