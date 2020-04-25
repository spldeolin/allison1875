package com.spldeolin.allison1875.da.deprecated.core.strategy;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * 如何从handler解析到返回类型，是可指定的
 *
 * @author Deolin 2020-01-02
 */
public interface ResponseBodyTypeParser {

    ResolvedType parse(MethodDeclaration handler);

}
