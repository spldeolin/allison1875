package com.spldeolin.allison1875.handlertransformer.dto;

import java.util.List;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author yanshaowei01 2026-02-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateServiceMethodRetval {

    MethodDeclaration method;

    List<String> neededImportsInImpl = Lists.newArrayList();

}