package com.spldeolin.allison1875.base.util.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;

/**
 * @author Deolin 2020-07-12
 */
public class Imports {

    public static void ensureImported(Node node, String importName) {
        if (importName.endsWith(".*")) {
            ensureImported(node, StringUtils.replaceLast(importName, ".*", ""), false, true);
        } else {
            ensureImported(node, importName, false, false);
        }
    }

    public static void ensureImported(Node node, String importName, boolean isStatic, boolean isAsterisk) {
        CompilationUnit cu = node.findCompilationUnit().orElseThrow(CuAbsentException::new);
        NodeList<ImportDeclaration> imports = cu.getImports();

        boolean noneMatch = true;
        for (ImportDeclaration anImport : imports) {
            if (anImport.getNameAsString().equals(importName) && anImport.isStatic() == isStatic
                    && anImport.isAsterisk() == isAsterisk) {
                noneMatch = false;
            }
        }
        if (noneMatch) {
            cu.addImport(importName, isStatic, isAsterisk);
        }
    }

}