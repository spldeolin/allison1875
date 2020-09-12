package com.spldeolin.allison1875.docanalyzer.strategy;

import com.spldeolin.allison1875.docanalyzer.dto.EnumCodeAndTitleDto;

/**
 * @author Deolin 2020-09-12
 */
public class DefaultAnalyzeEnumConstantStrategy implements AnalyzeEnumConstantStrategy {

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