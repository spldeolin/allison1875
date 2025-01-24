package com.spldeolin.allison1875.common.service;

import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.AnnotationExprServiceImpl;

/**
 * @author Deolin 2024-02-11
 */
@ImplementedBy(AnnotationExprServiceImpl.class)
public interface AnnotationExprService {

    boolean isAnnotated(String annoationQualifier, NodeWithAnnotations<?> node);

    Optional<AnnotationExpr> getAnnotation(String annoationQualifier, NodeWithAnnotations<?> node);

    AnnotationExpr lombokData();

    AnnotationExpr lombokAccessors();

    AnnotationExpr lomokFieldDefaultsPrivate();

    AnnotationExpr springService();

    AnnotationExpr lombokSlf4J();

    AnnotationExpr javaOverride();

    AnnotationExpr springRestController();

    AnnotationExpr springRequestMapping();

    AnnotationExpr springRequestBody();

    MarkerAnnotationExpr springRequestParamWithoutProperty();

    NormalAnnotationExpr springRequestParamWithProperty();

    AnnotationExpr springResponseBody();

    AnnotationExpr springDateTimeFormat();

    AnnotationExpr javaxValid();

    AnnotationExpr springAutowired();

    AnnotationExpr lombokEqualsAndHashCode();

    AnnotationExpr springController();

}