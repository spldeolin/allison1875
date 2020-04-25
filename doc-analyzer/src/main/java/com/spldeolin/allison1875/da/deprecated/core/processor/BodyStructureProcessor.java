package com.spldeolin.allison1875.da.deprecated.core.processor;

import static com.spldeolin.allison1875.da.deprecated.DocAnalyzerConfig.CONFIG;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.ast.ResolvedTypes;
import com.spldeolin.allison1875.base.util.exception.JsonSchemasException;
import com.spldeolin.allison1875.da.deprecated.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.NumberFormatTypeEnum;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
@Accessors(fluent = true)
class BodyStructureProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    @Setter
    private ResolvedType bodyType;

    @Setter
    protected Boolean forRequestBodyOrNot;

    protected Boolean inArray = false;

    protected Boolean inPage = false;

    BodyStructureEnum calcBodyStructure() {
        throw new IllegalStateException("Cannot call this method before calling process method.");
    }

    BodyStructureProcessor moreProcess(ApiDefinition api) {
        throw new IllegalStateException("Cannot call this method before calling process method.");
    }

    BodyStructureProcessor process() {
        checkStatus();

        if (bodyType == null) {
            return new VoidBodyProcessor();
        }

        BodyStructureProcessor result;
        try {
            if (isArray(bodyType)) {
                // 最外层是 数组
                result = tryProcessNonArrayLikeType(getArrayElementType(bodyType));
                result.inArray = true;
            } else if (isJucAndElementTypeExplicit(bodyType)) {
                // 最外层是 列表
                result = tryProcessNonArrayLikeType(getJUCElementType(bodyType));
                result.inArray = true;
            } else if (isPage(bodyType)) {
                // 最外层是 Page对象
                result = tryProcessNonArrayLikeType(getPageElementType(bodyType));
                result.inPage = true;
            } else {
                // 单层
                result = tryProcessNonArrayLikeType(bodyType);
            }
        } catch (Exception e) {
            log.warn("type={}, cause={}", bodyType.describe(), e.getMessage());
            // as mazy mode
            result = new ChaosBodyProcessor().jsonSchema(generateSchema(bodyType.describe()));
        }

        // 新对象调用fillProcessResultToApi时需要用到forRequestBodyOrNot属性
        return result.forRequestBodyOrNot(forRequestBodyOrNot);
    }

    private void checkStatus() {
        if (forRequestBodyOrNot == null) {
            throw new IllegalStateException("forRequestBodyOrNot cannot be absent.");
        }
    }

    private BodyStructureProcessor tryProcessNonArrayLikeType(ResolvedType type) {
        JsonSchema jsonSchema = generateSchema(type.describe());

        if (jsonSchema == null) {
            return new VoidBodyProcessor();

        } else if (jsonSchema.isObjectSchema()) {
            return new KeyValueBodyProcessor().objectSchema(jsonSchema.asObjectSchema());

        } else if (jsonSchema.isValueTypeSchema()) {
            FieldTypeEnum jsonType;
            NumberFormatTypeEnum numberFormat = null;
            if (jsonSchema.isStringSchema()) {
                jsonType = FieldTypeEnum.string;
            } else if (jsonSchema.isNumberSchema()) {
                jsonType = FieldTypeEnum.number;

                if (!jsonSchema.isIntegerSchema()) {
                    numberFormat = NumberFormatTypeEnum.f1oat;
                } else if (org.apache.commons.lang3.StringUtils
                        .equalsAny(type.describe(), QualifierConstants.INTEGER, "int")) {
                    numberFormat = NumberFormatTypeEnum.int32;
                } else if (org.apache.commons.lang3.StringUtils
                        .equalsAny(type.describe(), QualifierConstants.LONG, "long")) {
                    numberFormat = NumberFormatTypeEnum.int64;
                } else {
                    numberFormat = NumberFormatTypeEnum.inT;
                }
            } else if (jsonSchema.isBooleanSchema()) {
                jsonType = FieldTypeEnum.bool;
            } else {
                throw new RuntimeException("impossible unless bug");
            }
            return new ValueBodyProcessor().valueStructureJsonType(jsonType).valueStructureNumberFormat(numberFormat);

        } else {
            return new ChaosBodyProcessor().jsonSchema(jsonSchema);
        }
    }

    private boolean isArray(ResolvedType type) {
        return type.isArray();
    }

    private ResolvedType getArrayElementType(ResolvedType arrayType) {
        return arrayType.asArrayType().getComponentType();
    }

    private boolean isJucAndElementTypeExplicit(ResolvedType type) {
        if (type.isReferenceType()) {
            ResolvedReferenceType referenceType = type.asReferenceType();
            // is J.U.C
            if (ResolvedTypes.isOrLike(referenceType, QualifierConstants.COLLECTION)) {
                // is element type explicit or not
                return referenceType.getTypeParametersMap().size() == 1;
            }
        }
        return false;
    }

    private ResolvedType getJUCElementType(ResolvedType JUCType) {
        return Iterables.getOnlyElement(JUCType.asReferenceType().getTypeParametersMap()).b;
    }

    private boolean isPage(ResolvedType type) {
        if (type.isReferenceType()) {
            return type.asReferenceType().getId().equals(CONFIG.getCommonPageTypeQualifier());
        }
        return false;
    }

    private ResolvedType getPageElementType(ResolvedType pageType) {
        return Iterables.getOnlyElement(pageType.asReferenceType().getTypeParametersMap()).b;
    }

    private JsonSchema generateSchema(String resolvedTypeDescribe) {
        int typeParameterIndex = resolvedTypeDescribe.indexOf("<");
        if (typeParameterIndex != -1) {
            resolvedTypeDescribe = resolvedTypeDescribe.substring(0, typeParameterIndex);
        }

        JsonSchema result = null;
        try {
            result = JsonSchemaUtils.generateSchema(resolvedTypeDescribe);
        } catch (JsonSchemasException ignored) {
        }
        return result;
    }

}
