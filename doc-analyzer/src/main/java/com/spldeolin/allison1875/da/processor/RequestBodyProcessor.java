package com.spldeolin.allison1875.da.processor;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.da.builder.RequestBodyInfoBuilder;
import com.spldeolin.allison1875.da.dto.PropertiesContainerDto;
import com.spldeolin.allison1875.da.enums.BodySituationEnum;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class RequestBodyProcessor {

    private static final CommonBodyProcessor common = new CommonBodyProcessor();

    private final JsonSchemaGenerator jsg;

    public RequestBodyProcessor(JsonSchemaGenerator jsg) {
        this.jsg = jsg;
    }

    public RequestBodyInfoBuilder analyze(MethodDeclaration handler) {
        RequestBodyInfoBuilder requestBodyBuilder = new RequestBodyInfoBuilder();
        BodySituationEnum requestBodySituation;
        String requestBodyDescribe = null;
        try {
            ResolvedType requestBody = findRequestBody(handler);
            if (requestBody != null) {
                requestBodyDescribe = requestBody.describe();
                JsonSchema jsonSchema = JsonSchemaUtils.generateSchema(requestBodyDescribe, jsg);

                if (jsonSchema.isObjectSchema()) {
                    requestBodySituation = BodySituationEnum.KEY_VALUE;
                    PropertiesContainerDto propContainer = common
                            .anaylzeObjectSchema(requestBodyDescribe, jsonSchema.asObjectSchema());
                    requestBodyBuilder.flatRequestProperties(propContainer.getFlatProperties());
                } else if (common.fieldsAbsent(requestBody)) {
                    requestBodySituation = BodySituationEnum.NONE;
                } else {
                    requestBodySituation = BodySituationEnum.CHAOS;
                    requestBodyBuilder.requestBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                }
            } else {
                requestBodySituation = BodySituationEnum.NONE;
            }
        } catch (JsonSchemaException ignore) {
            requestBodySituation = BodySituationEnum.FAIL;
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), requestBodyDescribe, e);
            requestBodySituation = BodySituationEnum.FAIL;
        }
        requestBodyBuilder.requestBodySituation(requestBodySituation);
        return requestBodyBuilder;
    }

    /**
     * 1. 遍历出声明了@RequestBody的参数后返回
     * 2. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    private ResolvedType findRequestBody(MethodDeclaration method) {
        for (Parameter parameter : method.getParameters()) {
            Type type = parameter.getType();
            for (AnnotationExpr annotation : parameter.getAnnotations()) {
                try {
                    ResolvedAnnotationDeclaration resolve = annotation.resolve();
                    if (QualifierConstants.REQUEST_BODY.equals(resolve.getQualifiedName())) {
                        try {
                            return type.resolve();
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return null;
    }

}
