package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.javabean.BodyTypeAnalysisDto;
import com.spldeolin.allison1875.docanalyzer.service.EnumSchemaService;
import com.spldeolin.allison1875.docanalyzer.service.GetBodyResolvedTypeService;
import com.spldeolin.allison1875.docanalyzer.service.ReferenceSchemaService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.exception.JsonSchemaException;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class RequestBodyServiceImpl implements RequestBodyService {

    @Inject
    private GetBodyResolvedTypeService getBodyResolvedTypeService;

    @Inject
    private EnumSchemaService enumSchemaService;

    @Inject
    private ReferenceSchemaService referenceSchemaService;

    @Override
    public BodyTypeAnalysisDto analyze(JsonSchemaGenerator jsg, MethodDeclaration handler) {
        String requestBodyDescribe = null;
        try {
            ResolvedType requestBody = getBodyResolvedTypeService.getRequestBody(handler);
            if (requestBody != null) {
                if (requestBody.isPrimitive()) {
                    requestBodyDescribe = ((ResolvedPrimitiveType) requestBody).getBoxTypeQName();
                } else {
                    requestBodyDescribe = requestBody.describe();
                }
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(requestBodyDescribe, jsg);
                referenceSchemaService.resolve(jsonSchema);
                enumSchemaService.resolve(jsonSchema);
                return new BodyTypeAnalysisDto().setDescribe(requestBodyDescribe).setJsonSchema(jsonSchema);
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), requestBodyDescribe, e);
        }
        return new BodyTypeAnalysisDto();
    }

}
