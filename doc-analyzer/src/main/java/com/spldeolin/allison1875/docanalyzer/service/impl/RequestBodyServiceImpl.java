package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeBodyRetval;
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
    private JsonSchemaTransformerService jsonSchemaTransformerService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, MethodDeclaration mvcHandlerMd) {
        RequestBodyDTO requestBody = getRequestBodyType(mvcHandlerMd);
        if (requestBody == null) {
            return new AnalyzeBodyRetval();
        }

        String requestBodyDescribe;
        if (requestBody.type.isPrimitive()) {
            requestBodyDescribe = ((ResolvedPrimitiveType) requestBody.type).getBoxTypeQName();
        } else {
            requestBodyDescribe = requestBody.type.describe();
        }

        JsonSchema jsonSchema;
        try {
            jsonSchema = JsonSchemaGenerateUtils.generateSchema(requestBodyDescribe, jsg);
        } catch (Exception e) {
            log.error("fail to generateSchema, requestBodyDescribe={}", requestBodyDescribe);
            throw e;
        }
        jsonSchemaTransformerService.transformReferenceSchema(jsonSchema);

        List<String> desc = JavadocUtils.getTagDescriptionAsLines(mvcHandlerMd, Type.PARAM, requestBody.paramName);

        return new AnalyzeBodyRetval().setDescribe(requestBodyDescribe).setJsonSchema(jsonSchema)
                .setDescriptionLines(desc);
    }

    private RequestBodyDTO getRequestBodyType(MethodDeclaration mvcHandlerMd) {
        String name = MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd);

        List<Parameter> requestBodys = Lists.newArrayList();
        List<String> paramNames = Lists.newArrayList();
        for (Parameter param : mvcHandlerMd.getParameters()) {
            if (isRequestBody(param)) {
                requestBodys.add(param);
                paramNames.add(param.getNameAsString());
            }
        }

        if (requestBodys.isEmpty()) {
            return null;
        }

        if (requestBodys.size() > 1) {
            log.warn("mvcHanler '{}' unexpectedly has more than one @RequestBody param, only use first one", name);
        }

        Parameter onlyOrFirst = requestBodys.get(0);
        try {
            RequestBodyDTO retval = new RequestBodyDTO();
            retval.paramName = paramNames.get(0);
            retval.type = onlyOrFirst.getType().resolve();
            return retval;
        } catch (Exception e) {
            log.error("parameter '{}' of type cannot resolve", onlyOrFirst, e);
            return null;
        }
    }

    private boolean isRequestBody(Parameter parameter) {
        String qualifier = annotationExprService.springRequestBody().getNameAsString();
        return annotationExprService.isAnnotated(qualifier, parameter);
    }

    private static class RequestBodyDTO {

        String paramName;

        ResolvedType type;

    }

}
