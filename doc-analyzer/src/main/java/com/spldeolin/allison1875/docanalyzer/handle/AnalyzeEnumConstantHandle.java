package com.spldeolin.allison1875.docanalyzer.handle;

import javax.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.EnumCodeAndTitleDto;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class AnalyzeEnumConstantHandle {

    public boolean supportEnumType(Class<?> enumType) {
        return false;
    }

    public EnumCodeAndTitleDto analyzeEnumConstant(Object enumConstant) {
        // what ever
        return null;
    }

}