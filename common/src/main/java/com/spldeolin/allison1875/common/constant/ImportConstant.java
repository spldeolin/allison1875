package com.spldeolin.allison1875.common.constant;

import com.github.javaparser.ast.ImportDeclaration;

/**
 * @author Deolin 2021-01-24
 */
public interface ImportConstant {

    ImportDeclaration JAVA_UTIL = new ImportDeclaration("java.util", false, true);

    ImportDeclaration JAVA_TIME = new ImportDeclaration("java.time", false, true);

    ImportDeclaration GOOGLE_COMMON_COLLECTION = new ImportDeclaration("com.google.common.collect", false, true);

    ImportDeclaration APACHE_IBATIS = new ImportDeclaration("org.apache.ibatis.annotations", false, true);

    ImportDeclaration LOMBOK = new ImportDeclaration("lombok", false, true);

    ImportDeclaration LOMBOK_EXPERIMENTAL = new ImportDeclaration("lombok.experimental", false, true);

    ImportDeclaration LOMBOK_SLF4J = new ImportDeclaration("lombok.extern.slf4j.Slf4j", false, false);

    ImportDeclaration SPRING_SERVICE = new ImportDeclaration("org.springframework.stereotype.Service", false, false);

    ImportDeclaration SPRING_POST_MAPPING = new ImportDeclaration("org.springframework.web.bind.annotation.PostMapping",
            false, false);

    ImportDeclaration SPRING_REQUEST_BODY = new ImportDeclaration("org.springframework.web.bind.annotation.RequestBody",
            false, false);

    ImportDeclaration JAVAX_VALID = new ImportDeclaration("javax.validation.Valid", false, false);

    ImportDeclaration SPRING_AUTOWIRED = new ImportDeclaration("org.springframework.beans.factory.annotation.Autowired",
            false, false);

    ImportDeclaration SPRING_CONTROLLER = new ImportDeclaration("org.springframework.stereotype.Controller", false,
            false);

}
