package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeEnumConstantRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.EnumConstantAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(EnumConstantAnalyzerServiceImpl.class)
public interface EnumConstantAnalyzerService {

    boolean isSupport(Class<?> enumType);

    AnalyzeEnumConstantRetval analyzeEnumConstant(Object enumConstant);

}