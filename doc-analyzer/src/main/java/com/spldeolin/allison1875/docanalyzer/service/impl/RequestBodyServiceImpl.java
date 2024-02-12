package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.service.BodyResolvedTypeAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class RequestBodyServiceImpl implements RequestBodyService {

    @Inject
    private BodyResolvedTypeAnalyzerService bodyResolvedTypeAnalyzerService;

    @Inject
    private JsonSchemaTransformerService jsonSchemaTransformerService;

    @Override
    public AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, MethodDeclaration mvcHandlerMd) {
        String requestBodyDescribe = null;
        try {
            ResolvedType requestBody = bodyResolvedTypeAnalyzerService.analyzeRequestBody(mvcHandlerMd);
            if (requestBody != null) {
                if (requestBody.isPrimitive()) {
                    requestBodyDescribe = ((ResolvedPrimitiveType) requestBody).getBoxTypeQName();
                } else {
                    requestBodyDescribe = requestBody.describe();
                }
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(requestBodyDescribe, jsg);
                jsonSchemaTransformerService.transformReferenceSchema(jsonSchema);
                jsonSchemaTransformerService.transformForEnum(jsonSchema);
                return new AnalyzeBodyRetval().setDescribe(requestBodyDescribe).setJsonSchema(jsonSchema);
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd), requestBodyDescribe, e);
        }
        return new AnalyzeBodyRetval();
    }

}
