package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.service.impl.InitializerCollectServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(InitializerCollectServiceImpl.class)
public interface InitializerCollectService {

    List<InitializerDeclaration> collectInitializer(ClassOrInterfaceDeclaration coid);

}