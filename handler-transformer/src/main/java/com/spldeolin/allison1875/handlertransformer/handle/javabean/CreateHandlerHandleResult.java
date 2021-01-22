package com.spldeolin.allison1875.handlertransformer.handle.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-01-22
 */
@Data
@Accessors(chain = true)
public class CreateHandlerHandleResult {

    /**
     * handler方法
     */
    private MethodDeclaration handler;

    /**
     * 待追加的import声明
     */
    private Collection<String> appendImports;

}