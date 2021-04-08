package com.spldeolin.allison1875.docanalyzer.handle;

import javax.inject.Singleton;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2021-04-08
 */
@Singleton
public class DefaultMoreHandlerAnalysisHandle implements MoreHandlerAnalysisHandle {

    @Override
    public Object moreAnalysisFromMethod(HandlerFullDto handler) {
        return null;
    }

    @Override
    public String moreToString(Object dto) {
        return null;
    }

}