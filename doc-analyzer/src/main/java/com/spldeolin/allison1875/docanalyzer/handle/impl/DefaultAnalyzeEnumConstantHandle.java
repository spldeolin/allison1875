package com.spldeolin.allison1875.docanalyzer.handle.impl;

import com.spldeolin.allison1875.docanalyzer.dto.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;

/**
 * @author Deolin 2020-09-12
 */
public class DefaultAnalyzeEnumConstantHandle implements AnalyzeEnumConstantHandle {

    @Override
    public boolean supportEnumType(Class<?> enumType) {
        return false;
    }

    @Override
    public EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant) {
        // what ever
        return null;
    }

}