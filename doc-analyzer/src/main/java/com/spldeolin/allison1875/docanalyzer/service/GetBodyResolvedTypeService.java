package com.spldeolin.allison1875.docanalyzer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.GetBodyResolvedTypeServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(GetBodyResolvedTypeServiceImpl.class)
public interface GetBodyResolvedTypeService {

    /**
     * 1. 遍历出声明了@RequestBody的参数后返回
     * 2. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    ResolvedType getRequestBody(MethodDeclaration handler);

    /**
     * 1. controller上没有声明@RestController且handler上没有声明@ResponseBody时，认为没有ResponseBody
     * 2. 采用ConcernedResponseBodyTypeResolver提供的策略来获取ResponseBody
     * 3. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    ResolvedType getResponseBody(ClassOrInterfaceDeclaration controller, MethodDeclaration handler);

}