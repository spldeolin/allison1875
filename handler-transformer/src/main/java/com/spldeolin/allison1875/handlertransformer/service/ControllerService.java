package com.spldeolin.allison1875.handlertransformer.service;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.service.impl.ControllerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ControllerServiceImpl.class)
public interface ControllerService {

    Collection<ClassOrInterfaceDeclaration> collect(CompilationUnit cu);

    HandlerCreation createHandlerToController(FirstLineDto firstLineDto, ClassOrInterfaceDeclaration controller,
            ServiceGeneration serviceGeneration, ReqDtoRespDtoInfo reqDtoRespDtoInfo);

}