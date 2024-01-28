package com.spldeolin.allison1875.sqlapigenerator.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.AddMethodServiceImpl;

/**
 * @author Deolin 2024-01-22
 */
@ImplementedBy(AddMethodServiceImpl.class)
public interface AddMethodService {

    void addMethodToCoid(MethodDeclaration method, ClassOrInterfaceDeclaration coid);

    FileFlush addMethodToXml(List<String> xmlMethodCodeLines);

}