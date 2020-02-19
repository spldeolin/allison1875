package com.spldeolin.allison1875.da.core.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.da.core.util.Javadocs;

/**
 * @author Deolin 2020-02-19
 */
public class AuthorProcessor {

    public String process(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        String handlerAuthur = Javadocs.extractAuthorTag(handler);
        if (StringUtils.isEmpty(handlerAuthur)) {
            return Javadocs.extractAuthorTag(controller);
        }
        return handlerAuthur;
    }

}
