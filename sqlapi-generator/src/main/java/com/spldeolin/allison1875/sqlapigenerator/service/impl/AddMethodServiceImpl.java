package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.sqlapigenerator.SqlapiGeneratorConfig;
import com.spldeolin.allison1875.sqlapigenerator.service.AddMethodService;

/**
 * @author Deolin 2024-01-23
 */
@Singleton
public class AddMethodServiceImpl implements AddMethodService {

    private SqlapiGeneratorConfig config;

    @Override
    public void addMethodToCoid(MethodDeclaration method, ClassOrInterfaceDeclaration coid) {
        CompilationUnit cu = coid.findCompilationUnit().orElseThrow(() -> new CuAbsentException(coid));
        cu.addImport(ImportConstant.JAVA_UTIL);
        cu.addImport(ImportConstant.JAVA_TIME);
        coid.addMember(method);
    }

    @Override
    public FileFlush addMethodToXml(List<String> xmlMethodCodeLines) {
        return null;
    }

}