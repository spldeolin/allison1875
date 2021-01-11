package com.spldeolin.allison1875.htex.handle;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;

/**
 * @author Deolin 2021-01-10
 */
public interface CreateServiceMethodHandle {

    MethodDeclaration createMethodImpl(FirstLineDto firstLineDto, String paramType, String resultType);

}