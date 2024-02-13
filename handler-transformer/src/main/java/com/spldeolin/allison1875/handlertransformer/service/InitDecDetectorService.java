package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.service.impl.InitDecDetectorServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(InitDecDetectorServiceImpl.class)
public interface InitDecDetectorService {

    List<InitializerDeclaration> detectInitDecs(ClassOrInterfaceDeclaration mvcController);

}