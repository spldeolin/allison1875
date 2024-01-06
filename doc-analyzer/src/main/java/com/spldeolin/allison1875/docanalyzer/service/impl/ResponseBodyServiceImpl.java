package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.exception.JsonSchemaException;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class ResponseBodyServiceImpl implements ResponseBodyService {

    @Inject
    private EnumSchemaService enumSchemaService;

    @Inject
    private ReferenceSchemaService referenceSchemaService;

    @Inject
    private GetBodyResolvedTypeService getBodyResolvedTypeService;

    @Override
    public BodyTypeAnalysisDto analyze(JsonSchemaGenerator jsg, ClassOrInterfaceDeclaration controller,
            MethodDeclaration handler) {
        String responseBodyDescribe = null;
        try {
            ResolvedType responseBody = getBodyResolvedTypeService.getResponseBody(controller, handler);
            if (responseBody != null) {
                if (responseBody.isPrimitive()) {
                    responseBodyDescribe = ((ResolvedPrimitiveType) responseBody).getBoxTypeQName();
                } else {
                    responseBodyDescribe = responseBody.describe();
                }
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(responseBodyDescribe, jsg);
                referenceSchemaService.process(jsonSchema);
                enumSchemaService.process(jsonSchema);
                return new BodyTypeAnalysisDto().setDescribe(responseBodyDescribe).setJsonSchema(jsonSchema);
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), responseBodyDescribe, e);
        }
        return new BodyTypeAnalysisDto();
    }

}
