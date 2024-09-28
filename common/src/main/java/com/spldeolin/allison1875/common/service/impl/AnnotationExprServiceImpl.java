package com.spldeolin.allison1875.common.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-11
 */
@Singleton
@Slf4j
public class AnnotationExprServiceImpl implements AnnotationExprService {

    @Override
    public boolean isAnnotated(String annoationQualifier, NodeWithAnnotations<?> node) {
        for (AnnotationExpr annotation : node.getAnnotations()) {
            try {
                if (annoationQualifier.equals(annotation.resolve().getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("annotation '{}' of node '{}' cannot resolve", annotation.getNameAsString(), node, e);
                // 悲观地认为不匹配
            }
        }
        return false;
    }

    @Override
    public AnnotationExpr lombokData() {
        return StaticJavaParser.parseAnnotation("@lombok.Data").clone();
    }

    @Override
    public AnnotationExpr lombokAccessors() {
        return StaticJavaParser.parseAnnotation("@lombok.experimental.Accessors(chain = true)").clone();
    }

    @Override
    public AnnotationExpr lomokFieldDefaultsPrivate() {
        return StaticJavaParser.parseAnnotation(
                "@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PRIVATE)").clone();
    }

    @Override
    public AnnotationExpr springService() {
        return StaticJavaParser.parseAnnotation("@org.springframework.stereotype.Service").clone();
    }

    @Override
    public AnnotationExpr lombokSlf4J() {
        return StaticJavaParser.parseAnnotation("@lombok.extern.slf4j.Slf4j").clone();
    }

    @Override
    public AnnotationExpr javaOverride() {
        return StaticJavaParser.parseAnnotation("@Override").clone();
    }

    @Override
    public AnnotationExpr springRestController() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RestController").clone();
    }

    @Override
    public AnnotationExpr springRequestMapping() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestMapping").clone();
    }

    @Override
    public AnnotationExpr springRequestBody() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestBody").clone();
    }

    @Override
    public NormalAnnotationExpr springRequestParam() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestParam()")
                .asNormalAnnotationExpr().clone();
    }

    @Override
    public AnnotationExpr springResponseBody() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.ResponseBody").clone();
    }

    @Override
    public AnnotationExpr javaxValid() {
        return StaticJavaParser.parseAnnotation("@javax.validation.Valid").clone();
    }

    @Override
    public AnnotationExpr springAutowired() {
        return StaticJavaParser.parseAnnotation("@org.springframework.beans.factory.annotation.Autowired").clone();
    }

    @Override
    public AnnotationExpr lombokEqualsAndHashCode() {
        return StaticJavaParser.parseAnnotation("@lombok.EqualsAndHashCode(callSuper = true)").clone();
    }

    @Override
    public AnnotationExpr springController() {
        return StaticJavaParser.parseAnnotation("@org.springframework.stereotype.Controller").clone();
    }

}