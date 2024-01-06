package com.spldeolin.allison1875.handlertransformer.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.handlertransformer.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.CreateServiceMethodService;

/**
 * @author Deolin 2021-01-10
 */
@Singleton
public class CreateServiceMethodServiceImpl implements CreateServiceMethodService {

    @Override
    public CreateServiceMethodHandleResult createMethodImpl(FirstLineDto firstLineDto, String paramType,
            String resultType) {
        MethodDeclaration method = new MethodDeclaration();
        method.addAnnotation(AnnotationConstant.OVERRIDE);
        method.setPublic(true);
        if (resultType != null) {
            method.setType(resultType);
        } else {
            method.setType(new VoidType());
        }
        method.setName(firstLineDto.getHandlerName());
        if (paramType != null) {
            method.addParameter(paramType, "req");
        }

        BlockStmt body = new BlockStmt();
        if (resultType != null) {
            body.addStatement(StaticJavaParser.parseStatement("return null;"));
        }
        method.setBody(body);

        return new CreateServiceMethodHandleResult().setServiceMethod(method);
    }

}