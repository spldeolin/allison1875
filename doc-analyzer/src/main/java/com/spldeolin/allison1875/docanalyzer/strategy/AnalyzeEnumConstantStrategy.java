package com.spldeolin.allison1875.docanalyzer.strategy;

import com.spldeolin.allison1875.docanalyzer.dto.EnumCodeAndTitleDto;

/**
 * @author Deolin 2020-09-12
 */
public interface AnalyzeEnumConstantStrategy {

    boolean supportEnumType(Class<?> enumType);

    EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant);

}
