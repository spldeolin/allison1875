package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.service.BodyResolvedTypeAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class ResponseBodyServiceImpl implements ResponseBodyService {

    @Inject
    private JsonSchemaTransformerService jsonSchemaTransformerService;

    @Inject
    private BodyResolvedTypeAnalyzerService bodyResolvedTypeAnalyzerService;

    @Override
    public AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, ClassOrInterfaceDeclaration mvcControllerCoid,
            MethodDeclaration mvcHandlerMd) {
        String responseBodyDescribe = null;
        try {
            ResolvedType responseBody = bodyResolvedTypeAnalyzerService.analyzeResponseBody(mvcControllerCoid,
                    mvcHandlerMd);
            if (responseBody != null) {
                if (responseBody.isPrimitive()) {
                    responseBodyDescribe = ((ResolvedPrimitiveType) responseBody).getBoxTypeQName();
                } else {
                    responseBodyDescribe = responseBody.describe();
                }
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(responseBodyDescribe, jsg);
                jsonSchemaTransformerService.transformReferenceSchema(jsonSchema);
                jsonSchemaTransformerService.transformForEnum(jsonSchema);
                return new AnalyzeBodyRetval().setDescribe(responseBodyDescribe).setJsonSchema(jsonSchema);
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd), responseBodyDescribe, e);
        }
        return new AnalyzeBodyRetval();
    }

}
