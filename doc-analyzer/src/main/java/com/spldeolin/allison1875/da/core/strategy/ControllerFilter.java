package com.spldeolin.allison1875.da.core.strategy;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * @author Deolin 2020-02-21
 */
public interface ControllerFilter {

    boolean filter(ClassOrInterfaceDeclaration controller);

}
