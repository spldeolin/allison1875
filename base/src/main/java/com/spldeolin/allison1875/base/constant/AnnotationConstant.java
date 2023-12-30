package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * @author Deolin 2020-12-26
 */
public interface AnnotationConstant {

    AnnotationExpr DATA = StaticJavaParser.parseAnnotation("@Data");

    AnnotationExpr SERVICE = StaticJavaParser.parseAnnotation("@Service");

    AnnotationExpr SLF4J = StaticJavaParser.parseAnnotation("@Slf4j");

    AnnotationExpr OVERRIDE = StaticJavaParser.parseAnnotation("@Override");

    AnnotationExpr REQUEST_BODY = StaticJavaParser.parseAnnotation("@RequestBody");

    AnnotationExpr VALID = StaticJavaParser.parseAnnotation("@Valid");

    AnnotationExpr AUTOWIRED = StaticJavaParser.parseAnnotation("@Autowired");

    AnnotationExpr ACCESSORS = StaticJavaParser.parseAnnotation("@Accessors(chain = true)");

    AnnotationExpr FIELD_DEFAULTS_PRIVATE = StaticJavaParser.parseAnnotation(
            "@FieldDefaults(level = AccessLevel.PRIVATE)");

    AnnotationExpr EQUALS_AND_HASH_CODE = StaticJavaParser.parseAnnotation("@EqualsAndHashCode(callSuper = true)");

}
