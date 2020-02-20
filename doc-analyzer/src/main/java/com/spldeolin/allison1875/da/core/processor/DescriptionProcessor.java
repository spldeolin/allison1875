package com.spldeolin.allison1875.da.core.processor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.da.core.util.Javadocs;
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
        description = Javadocs.extractFirstLine(handler);
        return this;
    }

}
