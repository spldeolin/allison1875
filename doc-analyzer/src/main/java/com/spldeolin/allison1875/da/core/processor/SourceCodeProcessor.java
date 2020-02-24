package com.spldeolin.allison1875.da.core.processor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 源码位置
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
class SourceCodeProcessor {

    @Setter
    private MethodDeclaration handler;

    @Getter
    private String location;

    SourceCodeProcessor process() {
        checkStatus();

        location = Locations.getRelativePath(handler) + ":" + Locations.getBeginLine(handler.getName());
        return this;
    }

    private void checkStatus() {
        if (handler == null) {
            throw new IllegalStateException("handler cannot be absent.");
        }
    }

}
