package com.spldeolin.allison1875.base.util.ast;

import org.apache.commons.lang3.StringUtils;
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

}