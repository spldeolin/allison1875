package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.service.impl.ReqRespServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ReqRespServiceImpl.class)
public interface ReqRespService {

    void checkInitBody(BlockStmt initBody, FirstLineDto firstLineDto);

    ReqDtoRespDtoInfo createJavabeans(AstForest astForest, CompilationUnit cu, FirstLineDto firstLineDto,
            List<ClassOrInterfaceDeclaration> dtos);

}