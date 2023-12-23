package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.BodyTypeAnalysisDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.ResponseBodyServiceImpl;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ResponseBodyServiceImpl.class)
public interface ResponseBodyService {

    BodyTypeAnalysisDto analyze(JsonSchemaGenerator jsg, ClassOrInterfaceDeclaration controller,
            MethodDeclaration handler);

}
