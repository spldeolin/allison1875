package com.spldeolin.allison1875.base.constant;

import com.github.javaparser.ast.ImportDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Deolin 2021-01-24
 */
public interface ImportConstants {

    Multimap<String, String> a = ArrayListMultimap.create();

    ImportDeclaration COLLECTION = new ImportDeclaration("java.util.Collection", false, false);

    ImportDeclaration LIST = new ImportDeclaration("java.util.List", false, false);

    ImportDeclaration ARRAY_LIST = new ImportDeclaration("java.util.ArrayList", false, false);

    ImportDeclaration MAP = new ImportDeclaration("java.util.Map", false, false);

    ImportDeclaration MAPS = new ImportDeclaration("com.google.common.collect.Maps", false, false);

    ImportDeclaration MULTIMAP = new ImportDeclaration("com.google.common.collect.Multimap", false, false);

    ImportDeclaration ARRAY_LIST_MULTIMAP = new ImportDeclaration("com.google.common.collect.ArrayListMultimap", false,
            false);

    ImportDeclaration JAVA_UTIL = new ImportDeclaration("java.util", false, true);

    ImportDeclaration APACHE_IBATIS = new ImportDeclaration("org.apache.ibatis.annotations", false, true);

}
