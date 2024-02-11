package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.body.MethodDeclaration;
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

}