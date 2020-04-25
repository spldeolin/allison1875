package com.spldeolin.allison1875.da.deprecated.core.processor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 描述
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
class DescriptionProcessor {

    @Setter
    private MethodDeclaration handler;

    @Getter
    private String description;

    DescriptionProcessor process() {
        checkStatus();

        description = Javadocs.extractFirstLine(handler);
        return this;
    }

    private void checkStatus() {
        if (handler == null) {
            throw new IllegalStateException("handler cannot be absent.");
        }
    }

}
