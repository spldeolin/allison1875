package com.spldeolin.allison1875.da.core.strategy;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2020-01-06
 */
public class DefaultHandlerFilter implements HandlerFilter {

    @Override
    public boolean filter(ClassOrInterfaceDeclaration controller) {
        return true;
    }

    @Override
    public boolean filter(MethodDeclaration handler) {
        return true;
    }

}
