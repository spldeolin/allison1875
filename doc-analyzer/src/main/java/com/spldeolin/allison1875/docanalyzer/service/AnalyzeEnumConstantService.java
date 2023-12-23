package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.AnalyzeEnumConstantServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(AnalyzeEnumConstantServiceImpl.class)
public interface AnalyzeEnumConstantService {

    boolean supportEnumType(Class<?> enumType);

    EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant);

}