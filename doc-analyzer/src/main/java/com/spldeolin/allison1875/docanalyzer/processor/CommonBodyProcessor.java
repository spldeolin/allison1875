package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.IdUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.dto.EnumDto;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.dto.PropertiesContainerDto;
import com.spldeolin.allison1875.docanalyzer.dto.PropertyTreeNodeDto;
import com.spldeolin.allison1875.docanalyzer.enums.JsonTypeEnum;

/**
 * RequestMappingProcessor和ResponseBodyProcessor的共用代码
 *
 * @author Deolin 2020-06-10
 */
class CommonBodyProcessor {

    public PropertiesContainerDto anaylzeObjectSchema(String requestBodyDescribe, ObjectSchema objectSchema) {
        PropertyTreeNodeDto tempParent = new PropertyTreeNodeDto();
        calcObjectTypeWithRecur(tempParent, objectSchema, false);
        List<PropertyTreeNodeDto> dendriformProperties = getDendriformPropertiesFromTemp(tempParent);
        return new PropertiesContainerDto(requestBodyDescribe, dendriformProperties);
    }

    public List<PropertyTreeNodeDto> getDendriformPropertiesFromTemp(PropertyTreeNodeDto tempParent) {
        return tempParent.getChildren().stream().map(child -> child.setParent(null)).collect(Collectors.toList());
    }

    public JsonTypeEnum calcObjectTypeWithRecur(PropertyTreeNodeDto parent, ObjectSchema parentSchema,
            boolean isInArray) {
        parent.setJsonType(isInArray ? JsonTypeEnum.OBJECT_ARRAY : JsonTypeEnum.OBJECT);

        Collection<PropertyTreeNodeDto> children = Lists.newLinkedList();
        for (Entry<String, JsonSchema> entry : parentSchema.getProperties().entrySet()) {
            String childName = entry.getKey();
            JsonSchema childSchema = entry.getValue();
            PropertyTreeNodeDto child = new PropertyTreeNodeDto();
            child.setId(IdUtils.nextId());
            child.setName(childName);

            JsonPropertyDescriptionValueDto jpdv = null;
            try {
                jpdv = JsonUtils.toObject(childSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
            } catch (Exception ignored) {
            }

            JsonTypeEnum jsonType;
            Boolean isFloat = null;
            Boolean isEnum = null;
            Collection<EnumDto> enums = null;
            if (childSchema.isValueTypeSchema()) {
                ValueTypeSchema valueSchema = childSchema.asValueTypeSchema();
                jsonType = calcValueType(valueSchema, false);
                isFloat = isFloat(valueSchema);
                isEnum = isEnum(valueSchema);
                if (isEnum) {
                    enums = calcEnum(valueSchema);
                }
            } else if (childSchema.isObjectSchema()) {
                jsonType = calcObjectTypeWithRecur(child, childSchema.asObjectSchema(), false);
            } else if (childSchema.isArraySchema()) {
                Items items = childSchema.asArraySchema().getItems();
                if (items == null || items.isArrayItems()) {
                    jsonType = JsonTypeEnum.UNKNOWN;
                } else {
                    JsonSchema eleSchema = items.asSingleItems().getSchema();
                    if (eleSchema.isValueTypeSchema()) {
                        ValueTypeSchema valueSchema = eleSchema.asValueTypeSchema();
                        jsonType = calcValueType(valueSchema, true);
                        isFloat = isFloat(valueSchema);
                        isEnum = isEnum(valueSchema);
                        if (isEnum) {
                            enums = calcEnum(valueSchema);
                        }
                    } else if (eleSchema.isObjectSchema()) {
                        jsonType = calcObjectTypeWithRecur(child, eleSchema.asObjectSchema(), true);
                    } else if (eleSchema instanceof ReferenceSchema) {
                        jsonType = JsonTypeEnum.REFERENCE_ARRAY;
                    } else {
                        jsonType = JsonTypeEnum.UNKNOWN;
                    }
                }
            } else if (childSchema instanceof ReferenceSchema) {
                jsonType = JsonTypeEnum.REFERENCE;
            } else {
                jsonType = JsonTypeEnum.UNKNOWN;
            }
            child.setJsonType(jsonType);
            child.setIsFloat(isFloat);
            child.setIsEnum(isEnum);
            child.setEnums(enums);

            if (jpdv != null) {
                child.setDescription(jpdv.getDescription());
                child.setValidators(jpdv.getValidators());
                child.setDatetimePattern(jpdv.getJsonFormatPattern());
            }

            child.setParent(parent);
            if (child.getChildren() == null) {
                child.setChildren(Lists.newArrayList());
            }
            children.add(child);
        }

        parent.setChildren(children);
        return parent.getJsonType();
    }


    public JsonTypeEnum calcValueType(ValueTypeSchema vSchema, boolean isInArray) {
        if (vSchema.isNumberSchema()) {
            return isInArray ? JsonTypeEnum.NUMBER_ARRAY : JsonTypeEnum.NUMBER;
        } else if (vSchema.isStringSchema()) {
            return isInArray ? JsonTypeEnum.STRING_ARRAY : JsonTypeEnum.STRING;
        } else if (vSchema.isBooleanSchema()) {
            return isInArray ? JsonTypeEnum.BOOLEAN_ARRAY : JsonTypeEnum.BOOLEAN;
        } else {
            throw new IllegalArgumentException(vSchema.toString());
        }
    }


    public Boolean isFloat(ValueTypeSchema valueTypeSchema) {
        if (valueTypeSchema.isIntegerSchema()) {
            return false;
        } else if (valueTypeSchema.isNumberSchema()) {
            return true;
        } else {
            return null;
        }
    }

    public Boolean isEnum(ValueTypeSchema valueSchema) {
        return !CollectionUtils.isEmpty(valueSchema.getEnums());
    }

    public Collection<EnumDto> calcEnum(ValueTypeSchema valueSchema) {
        Collection<EnumDto> result = Lists.newArrayList();
        for (String enumJson : valueSchema.getEnums()) {
            EnumDto cad = JsonUtils.toObject(enumJson, EnumDto.class);
            result.add(cad);
        }
        return result;
    }

    public boolean fieldsAbsent(ResolvedType requestBody) {
        if (requestBody.isReferenceType()) {
            return requestBody.asReferenceType().getDeclaredFields().size() == 0;
        }
        return false;
    }

}
