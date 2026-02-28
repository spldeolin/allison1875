package com.spldeolin.allison1875.common.service.impl;

import java.util.Optional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-11
 */
@Singleton
@Slf4j
public class AnnotationExprServiceImpl implements AnnotationExprService {

    @Inject
    private CommonConfig commonConfig;

    @Override
    public boolean isAnnotated(String annoationQualifier, NodeWithAnnotations<?> node) {
        return getAnnotation(annoationQualifier, node).isPresent();
    }

    @Override
    public Optional<AnnotationExpr> getAnnotation(String annoationQualifier, NodeWithAnnotations<?> node) {
        for (AnnotationExpr annotation : node.getAnnotations()) {
            try {
                if (annoationQualifier.equals(annotation.resolve().getQualifiedName())) {
                    return Optional.of(annotation);
                }
            } catch (Exception e) {
                log.warn("annotation '{}' of node '{}' cannot resolve", annotation.getNameAsString(), node, e);
                // 悲观地认为不匹配
            }
        }
        return Optional.empty();
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
    public AnnotationExpr lombokFieldDefaultsPrivate() {
        return StaticJavaParser.parseAnnotation(
                "@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PRIVATE)").clone();
    }

    @Override
    public AnnotationExpr lombokGetter() {
        return StaticJavaParser.parseAnnotation("@lombok.Getter").clone();
    }

    @Override
    public AnnotationExpr lombokAllArgsConstructor() {
        return StaticJavaParser.parseAnnotation("@lombok.AllArgsConstructor").clone();
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
    public AnnotationExpr springRequestMapping(String path) {
        return StaticJavaParser.parseAnnotation(
                String.format("@org.springframework.web.bind.annotation.RequestMapping(\"%s\")", path)).clone();
    }

    @Override
    public AnnotationExpr springRequestBody() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestBody").clone();
    }

    @Override
    public MarkerAnnotationExpr springRequestParamWithoutProperty() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestParam")
                .asMarkerAnnotationExpr().clone();
    }

    @Override
    public NormalAnnotationExpr springRequestParamWithProperty() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.RequestParam()")
                .asNormalAnnotationExpr().clone();
    }

    @Override
    public AnnotationExpr springResponseBody() {
        return StaticJavaParser.parseAnnotation("@org.springframework.web.bind.annotation.ResponseBody").clone();
    }

    @Override
    public AnnotationExpr springDateTimeFormat() {
        return StaticJavaParser.parseAnnotation(
                "@org.springframework.format.annotation.DateTimeFormat(pattern=\"yyyy-MM-dd HH:mm:ss\")").clone();
    }

    @Override
    public AnnotationExpr javaxValid() {
        if (commonConfig.getEnableJavaxMoveToJakarta()) {
            return StaticJavaParser.parseAnnotation("@jakarta.validation.Valid").clone();
        } else {
            return StaticJavaParser.parseAnnotation("@javax.validation.Valid").clone();
        }
    }

    @Override
    public AnnotationExpr notNull() {
        if (commonConfig.getEnableJavaxMoveToJakarta()) {
            return StaticJavaParser.parseAnnotation("@jakarta.validation.constraints.NotNull").clone();
        } else {
            return StaticJavaParser.parseAnnotation("@javax.validation.constraints.NotNull").clone();
        }
    }

    @Override
    public AnnotationExpr notEmpty() {
        if (commonConfig.getEnableJavaxMoveToJakarta()) {
            return StaticJavaParser.parseAnnotation("@jakarta.validation.constraints.NotEmpty").clone();
        } else {
            return StaticJavaParser.parseAnnotation("@javax.validation.constraints.NotEmpty").clone();
        }
    }

    @Override
    public AnnotationExpr notBlank() {
        if (commonConfig.getEnableJavaxMoveToJakarta()) {
            return StaticJavaParser.parseAnnotation("@jakarta.validation.constraints.NotBlank").clone();
        } else {
            return StaticJavaParser.parseAnnotation("@javax.validation.constraints.NotBlank").clone();
        }
    }

    @Override
    public AnnotationExpr size(int min, int max) {
        String args = "(";
        if (min > 0) {
            args += "min=" + min + ", ";
        }
        args += "max=" + max + ")";
        if (commonConfig.getEnableJavaxMoveToJakarta()) {
            return StaticJavaParser.parseAnnotation("@jakarta.validation.constraints.Size" + args).clone();
        } else {
            return StaticJavaParser.parseAnnotation("@javax.validation.constraints.Size" + args).clone();
        }
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

    @Override
    public AnnotationExpr springTransactional() {
        return StaticJavaParser.parseAnnotation("@org.springframework.transaction.annotation.Transactional").clone();
    }

}