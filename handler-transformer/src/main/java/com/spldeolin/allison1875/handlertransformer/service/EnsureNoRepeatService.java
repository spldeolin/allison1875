package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.EnsureNoRepeatServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(EnsureNoRepeatServiceImpl.class)
public interface EnsureNoRepeatService {

    /**
     * 确保参数firstLineDto中的handlerName与controller中所有的controller均不重名
     */
    void inController(ClassOrInterfaceDeclaration controller, FirstLineDto firstLineDto);

    /**
     * 确保参数methodName与service中所有的方法均不重名
     */
    String inService(ClassOrInterfaceDeclaration service, String methodName);

    /**
     * 确保参数coidName与AST森林中所有的java文件名均不重名
     */
    String inAstForest(AstForest astForest, String coidName);

}