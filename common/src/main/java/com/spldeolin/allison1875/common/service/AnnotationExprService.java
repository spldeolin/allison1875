package com.spldeolin.allison1875.common.service;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.AnnotationExprServiceImpl;

/**
 * @author Deolin 2024-02-11
 */
@ImplementedBy(AnnotationExprServiceImpl.class)
public interface AnnotationExprService {

    AnnotationExpr lombokData();

    AnnotationExpr lombokAccessors();

    AnnotationExpr lomokFieldDefaultsPrivate();

    AnnotationExpr springService();

    AnnotationExpr lombokSlf4J();

    AnnotationExpr javaOverride();

    AnnotationExpr springRequestbody();

    AnnotationExpr javaxValid();

    AnnotationExpr springAutowired();

    AnnotationExpr lombokEqualsAndHashCode();

    AnnotationExpr springController();

}