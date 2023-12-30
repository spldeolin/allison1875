package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;

/**
 * @author Deolin 2021-01-24
 */
public interface ImportConstant {

    ImportDeclaration JAVA_UTIL = StaticJavaParser.parseImport("java.util.*");

    ImportDeclaration JAVA_TIME = StaticJavaParser.parseImport("java.time.*");

    ImportDeclaration GOOGLE_COMMON_COLLECTION = StaticJavaParser.parseImport("com.google.common.collect.*");

    ImportDeclaration APACHE_IBATIS = StaticJavaParser.parseImport("org.apache.ibatis.annotations.*");

    ImportDeclaration LOMBOK = StaticJavaParser.parseImport("lombok.*");

    ImportDeclaration LOMBOK_EXPERIMENTAL = StaticJavaParser.parseImport("lombok.experimental.*");

    ImportDeclaration LOMBOK_SLF4J = StaticJavaParser.parseImport("lombok.extern.slf4j.Slf4j");

    ImportDeclaration SPRING_SERVICE = StaticJavaParser.parseImport("org.springframework.stereotype.Service");

    ImportDeclaration SPRING_POST_MAPPING = StaticJavaParser.parseImport(
            "org.springframework.web.bind.annotation.PostMapping");

    ImportDeclaration SPRING_REQUEST_BODY = StaticJavaParser.parseImport(
            "org.springframework.web.bind.annotation.RequestBody");

    ImportDeclaration JAVAX_VALID = StaticJavaParser.parseImport("javax.validation.Valid");

    ImportDeclaration SPRING_AUTOWIRED = StaticJavaParser.parseImport(
            "org.springframework.beans.factory.annotation.Autowired");

    ImportDeclaration SPRING_CONTROLLER = StaticJavaParser.parseImport("org.springframework.stereotype.Controller");

}
