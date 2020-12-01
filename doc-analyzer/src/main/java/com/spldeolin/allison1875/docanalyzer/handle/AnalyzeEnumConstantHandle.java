package com.spldeolin.allison1875.docanalyzer.handle;

import com.spldeolin.allison1875.docanalyzer.dto.EnumCodeAndTitleDto;

/**
 * @author Deolin 2020-09-12
 */
public interface AnalyzeEnumConstantHandle {

    boolean supportEnumType(Class<?> enumType);

    EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant);

}
