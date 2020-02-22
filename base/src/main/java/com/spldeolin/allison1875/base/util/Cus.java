package com.spldeolin.allison1875.base.util;

import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * @author Deolin 2020-02-22
 */
public class Cus {

    public static String getAuthor(CompilationUnit cu) {
        Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
        if (primaryType.isPresent()) {
            return Javadocs.extractAuthorTag(primaryType.get());
        }
        return "";
    }

}
