package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.MethodCollectServiceImpl;

/**
 * 内聚了 收集coid下所有method的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MethodCollectServiceImpl.class)
public interface MethodCollectService {

    Map<String, MethodDeclaration> collectMethods(ClassOrInterfaceDeclaration coid);

}
