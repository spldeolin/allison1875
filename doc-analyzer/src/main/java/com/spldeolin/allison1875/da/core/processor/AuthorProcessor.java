package com.spldeolin.allison1875.da.core.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 作者
 *
 * @author Deolin 2020-02-19
 */
@Accessors(fluent = true)
class AuthorProcessor {

    @Setter
    private ClassOrInterfaceDeclaration controller;

    @Setter
    private MethodDeclaration handler;

    @Getter
    private String author;

    AuthorProcessor process() {
        checkStatus();

        author = Javadocs.extractAuthorTag(handler);
        if (StringUtils.isEmpty(author)) {
            author = Javadocs.extractAuthorTag(controller);
        }
        return this;
    }

    private void checkStatus() {
        if (controller == null) {
            throw new IllegalStateException("controller cannot be absent.");
        }
        if (handler == null) {
            throw new IllegalStateException("handler cannot be absent.");
        }
    }

}
