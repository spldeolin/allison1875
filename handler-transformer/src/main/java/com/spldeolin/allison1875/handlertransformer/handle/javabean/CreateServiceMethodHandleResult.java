package com.spldeolin.allison1875.handlertransformer.handle.javabean;

import java.util.Collection;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-01-22
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateServiceMethodHandleResult {

    MethodDeclaration serviceMethod;

    /**
     * 待追加的import声明
     */
    Collection<String> appendImports = Lists.newArrayList();

}