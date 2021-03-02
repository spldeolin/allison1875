package com.spldeolin.allison1875.handlertransformer.handle.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-01-22
 */
@Data
@Accessors(chain = true)
public class CreateServiceMethodHandleResult {

    private MethodDeclaration serviceMethod;

    /**
     * 待追加的import声明
     */
    private Collection<String> appendImports = Lists.newArrayList();

}