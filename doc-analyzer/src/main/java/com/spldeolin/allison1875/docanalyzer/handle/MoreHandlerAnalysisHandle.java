package com.spldeolin.allison1875.docanalyzer.handle;

import java.lang.reflect.Field;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2021-04-08
 */
public interface MoreHandlerAnalysisHandle {

    Object moreAnalysisFromMethod(HandlerFullDto handler);

    String moreToString(Object dto);

}
