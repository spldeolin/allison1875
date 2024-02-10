package com.spldeolin.allison1875.common.constant;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * @author Deolin 2020-12-26
 */
public interface AnnotationConstant {

    AnnotationExpr DATA = StaticJavaParser.parseAnnotation("@Data");

    AnnotationExpr DATA_FULL = StaticJavaParser.parseAnnotation("@lombok.Data");

    AnnotationExpr SERVICE = StaticJavaParser.parseAnnotation("@Service");

    AnnotationExpr SERVICE_FULL = StaticJavaParser.parseAnnotation("@org.springframework.stereotype.Service");

    AnnotationExpr SLF4J = StaticJavaParser.parseAnnotation("@Slf4j");

    AnnotationExpr SLF4J_FULL = StaticJavaParser.parseAnnotation("@lombok.extern.slf4j.Slf4j");

    AnnotationExpr OVERRIDE = StaticJavaParser.parseAnnotation("@Override");

    AnnotationExpr REQUEST_BODY = StaticJavaParser.parseAnnotation("@RequestBody");

    AnnotationExpr REQUEST_BODY_FULL = StaticJavaParser.parseAnnotation(
            "@org.springframework.web.bind.annotation.RequestBody");

    AnnotationExpr VALID = StaticJavaParser.parseAnnotation("@Valid");

    AnnotationExpr VALID_FULL = StaticJavaParser.parseAnnotation("@javax.validation.Valid");

    AnnotationExpr AUTOWIRED = StaticJavaParser.parseAnnotation("@Autowired");

    AnnotationExpr AUTOWIRED_FULL = StaticJavaParser.parseAnnotation(
            "@org.springframework.beans.factory.annotation.Autowired");

    AnnotationExpr ACCESSORS = StaticJavaParser.parseAnnotation("@Accessors(chain = true)");

    AnnotationExpr ACCESSORS_FULL = StaticJavaParser.parseAnnotation("@lombok.experimental.Accessors(chain = true)");

    AnnotationExpr FIELD_DEFAULTS_PRIVATE = StaticJavaParser.parseAnnotation(
            "@FieldDefaults(level = AccessLevel.PRIVATE)");

    AnnotationExpr FIELD_DEFAULTS_PRIVATE_FULL = StaticJavaParser.parseAnnotation(
            "@lombok.experimental.FieldDefaults(level = AccessLevel.PRIVATE)");

    AnnotationExpr EQUALS_AND_HASH_CODE = StaticJavaParser.parseAnnotation("@EqualsAndHashCode(callSuper = true)");

    AnnotationExpr EQUALS_AND_HASH_CODE_FULL = StaticJavaParser.parseAnnotation(
            "@lombok.EqualsAndHashCode(callSuper = true)");

}
