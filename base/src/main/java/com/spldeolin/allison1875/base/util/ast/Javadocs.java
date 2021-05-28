package com.spldeolin.allison1875.base.util.ast;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.base.MoreObjects;

/**
 * Javadoc工具类
 *
 * @author Deolin 2021-03-05
 */
public class Javadocs {

    private Javadocs() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static Javadoc createJavadoc(String comment, String author) {
        if (StringUtils.isBlank(author)) {
            throw new IllegalArgumentException("author cannot be blank.");
        }
        comment = MoreObjects.firstNonNull(comment, "");
        Javadoc javadoc = new JavadocComment(comment).parse()
                .addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
        return javadoc;
    }

    public static void main(String[] args) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser
                .parse(new File("/Users/deolin/Documents/源码工具新版Design类/SmarthrProjectDesign.java"));


        TypeDeclaration<?> pt = cu.getPrimaryType().get();


        System.out.println(1);
    }

}