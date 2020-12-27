package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * @author Deolin 2020-12-26
 */
public interface AnnotationConstant {

    AnnotationExpr DATA = StaticJavaParser.parseAnnotation("@Data");

}
