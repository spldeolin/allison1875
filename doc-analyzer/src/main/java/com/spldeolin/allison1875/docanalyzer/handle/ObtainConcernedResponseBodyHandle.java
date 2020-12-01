package com.spldeolin.allison1875.docanalyzer.handle;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * 获取受关注的ResponseBody类型的策略
 *
 * @author Deolin 2020-06-18
 */
public interface ObtainConcernedResponseBodyHandle {

    ResolvedType findConcernedResponseBodyType(MethodDeclaration handler);

}
