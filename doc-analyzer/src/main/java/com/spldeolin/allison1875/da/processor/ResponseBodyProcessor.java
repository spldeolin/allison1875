package com.spldeolin.allison1875.da.processor;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.da.strategy.ConcernedResponseBodyTypeStrategy;
import com.spldeolin.allison1875.da.builder.ResponseBodyInfoResult;
import com.spldeolin.allison1875.da.dto.PropertiesContainerDto;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.enums.BodySituationEnum;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ResponseBodyProcessor {

    private final AstForest astForest;

    private final JsonSchemaGenerator jsg;

    private final CommonBodyProcessor common = new CommonBodyProcessor();

    public ResponseBodyProcessor(AstForest astForest, JsonSchemaGenerator jsg) {
        this.astForest = astForest;
        this.jsg = jsg;
    }

    public ResponseBodyInfoResult analyze(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        ResponseBodyInfoResult responseBodyInfo = new ResponseBodyInfoResult();
        BodySituationEnum responseBodySituation;
        String responseBodyDescribe = null;
        try {
            ResolvedType responseBody = findResponseBody(controller, handler);
            if (responseBody != null) {
                responseBodyDescribe = responseBody.describe();
                JsonSchema jsonSchema = JsonSchemaUtils
                        .generateSchema(responseBodyDescribe, astForest.getCurrentClassLoader(), jsg);

                if (jsonSchema.isArraySchema()) {
                    Items items = jsonSchema.asArraySchema().getItems();
                    if (items != null && items.isSingleItems() && items.asSingleItems().getSchema().isObjectSchema()) {
                        responseBodySituation = BodySituationEnum.KEY_VALUE_ARRAY;
                        PropertiesContainerDto propContainer = common.anaylzeObjectSchema(responseBodyDescribe,
                                items.asSingleItems().getSchema().asObjectSchema());
                        clearAllValidatorAndNullableFlag(propContainer);
                        responseBodyInfo.flatResponseProperties(propContainer.getFlatProperties());
                    } else {
                        responseBodySituation = BodySituationEnum.CHAOS;
                        responseBodyInfo.responseBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                    }
                } else if (jsonSchema.isObjectSchema()) {
                    responseBodySituation = BodySituationEnum.KEY_VALUE;
                    PropertiesContainerDto propContainer = common
                            .anaylzeObjectSchema(responseBodyDescribe, jsonSchema.asObjectSchema());
                    clearAllValidatorAndNullableFlag(propContainer);
                    responseBodyInfo.flatResponseProperties(propContainer.getFlatProperties());
                } else if (common.fieldsAbsent(responseBody)) {
                    responseBodySituation = BodySituationEnum.NONE;
                } else {
                    responseBodySituation = BodySituationEnum.CHAOS;
                    responseBodyInfo.responseBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                }
            } else {
                responseBodySituation = BodySituationEnum.NONE;
            }
        } catch (JsonSchemaException ignore) {
            responseBodySituation = BodySituationEnum.FAIL;
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), responseBodyDescribe, e);
            responseBodySituation = BodySituationEnum.FAIL;
        }
        responseBodyInfo.responseBodySituation(responseBodySituation);
        return responseBodyInfo;
    }

    /**
     * 1. controller上没有声明@RestController且handler上没有声明@ResponseBody时，认为没有ResponseBody
     * 2. 采用ConcernedResponseBodyTypeResolver提供的策略来获取ResponseBody
     * 3. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    private ResolvedType findResponseBody(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        try {
            if (Annotations.isAnnoAbsent(controller, RestController.class) && Annotations
                    .isAnnoAbsent(handler, ResponseBody.class)) {
                return null;
            }
            return new ConcernedResponseBodyTypeStrategy().findConcernedResponseBodyType(handler);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    private void clearAllValidatorAndNullableFlag(PropertiesContainerDto propContainer) {
        for (PropertyDto prop : propContainer.getFlatProperties()) {
            prop.setRequired(null);
            prop.setValidators(null);
        }
    }

}