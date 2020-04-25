package com.spldeolin.allison1875.da.deprecated.core.strategy;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2020-01-02
 */
public interface HandlerFilter {

    boolean filter(MethodDeclaration controller);

}
