package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
public class HandlerCreation {

    /**
     * handler方法
     */
    MethodDeclaration handler;

    /**
     * 待追加的Controller类的注解
     */
    final List<AnnotationExpr> appendAnnotations4Controller = Lists.newArrayList();

}