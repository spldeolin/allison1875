package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * @author Deolin 2020-12-26
 */

public interface AnnotationConstant {

    AnnotationExpr DATA = StaticJavaParser.parseAnnotation("@Data");

    String DATA_QUALIFIER = "lombok.Data";

    AnnotationExpr SERVICE = StaticJavaParser.parseAnnotation("@Service");

    String SERVICE_QUALIFIER = "org.springframework.stereotype.Service";

    AnnotationExpr SLF4J = StaticJavaParser.parseAnnotation("@Slf4j");

    String SLF4J_QUALIFIER = "lombok.extern.slf4j.Slf4j";

    AnnotationExpr OVERRIDE = StaticJavaParser.parseAnnotation("@Override");

    String POST_MAPPING_QUALIFIER = "org.springframework.web.bind.annotation.PostMapping";

    AnnotationExpr REQUEST_BODY = StaticJavaParser.parseAnnotation("@RequestBody");

    String REQUEST_BODY_QUALIFIER = "org.springframework.web.bind.annotation.RequestBody";

    AnnotationExpr VALID = StaticJavaParser.parseAnnotation("@Valid");

    String VALID_QUALIFIER = "javax.validation.Valid";


    AnnotationExpr AUTOWIRED = StaticJavaParser.parseAnnotation("@Autowired");

    String AUTOWIRED_QUALIFIER = "org.springframework.beans.factory.annotation.Autowired";

}
