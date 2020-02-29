package com.spldeolin.allison1875.da.core.processor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.util.ast.Authors;
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
    private MethodDeclaration handler;

    @Getter
    private String author;

    AuthorProcessor process() {
        checkStatus();

        author = Authors.getAuthor(handler);
        return this;
    }

    private void checkStatus() {
        if (handler == null) {
            throw new IllegalStateException("handler cannot be absent.");
        }
    }

}
