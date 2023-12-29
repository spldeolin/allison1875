package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.ast.ImportDeclaration;

/**
 * @author Deolin 2021-01-24
 */
public interface ImportConstant {

    ImportDeclaration COLLECTION = new ImportDeclaration("java.util.Collection", false, false);

    ImportDeclaration JAVA_UTIL = new ImportDeclaration("java.util", false, true);

    ImportDeclaration APACHE_IBATIS = new ImportDeclaration("org.apache.ibatis.annotations", false, true);

}
