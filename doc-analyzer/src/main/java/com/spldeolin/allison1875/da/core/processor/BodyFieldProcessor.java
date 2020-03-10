package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Jsons;
import com.spldeolin.allison1875.da.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-03
 */
@Log4j2
@Accessors(fluent = true)
class BodyFieldProcessor {

    @Setter
    private ObjectSchema objectSchema;

    @Getter
    private Collection<BodyFieldDefinition> firstFloorFields;

    BodyFieldProcessor process() {
        checkStatus();
        firstFloorFields = parseFieldTypes(objectSchema, false, new BodyFieldDefinition()).getChildFields();
        firstFloorFields.forEach(fieldDto -> fieldDto.setParentField(null));
        return this;
    }

    private void checkStatus() {
        if (objectSchema == null) {
            throw new IllegalStateException("objectSchema cannot be absent.");
        }
    }

    private BodyFieldDefinition parseFieldTypes(ObjectSchema schema, boolean isObjectInArray,
            BodyFieldDefinition parent) {
        if (isObjectInArray) {
            parent.setJsonType(FieldTypeEnum.objectArray);
        } else {
            parent.setJsonType(FieldTypeEnum.object);
        }

        List<BodyFieldDefinition> children = Lists.newArrayList();
        schema.getProperties().forEach((childName, childSchema) -> {
            BodyFieldDefinition child;
            if (StringUtils.isNotEmpty(childSchema.getDescription())) {
                child = Jsons.toObject(childSchema.getDescription(), BodyFieldDefinition.class);
            } else {
                log.warn("Cannot found JsonPropertyDescription, schema=[{}], field=[{}]", schema.getId(), childName);
                child = new BodyFieldDefinition();
            }

            child.setFieldName(childName);

            if (childSchema.isValueTypeSchema()) {
                child.setJsonType(calcValueDataType(childSchema.asValueTypeSchema(), false));
            } else if (childSchema.isObjectSchema()) {
                parseFieldTypes(childSchema.asObjectSchema(), false, child);
            } else if (childSchema.isArraySchema()) {
                ArraySchema aSchema = childSchema.asArraySchema();
                if (aSchema.getItems() == null) {
                    log.warn("Cannot analyze the type of array element, schema=[{}], field=[{}]", schema.getId(),
                            childName);
                    return;
                }
                if (aSchema.getItems().isArrayItems()) {
                    log.warn("Cannot analyze the type of array element, schema=[{}], field=[{}]", schema.getId(),
                            childName);
                    return;
                }
                JsonSchema eleSchema = aSchema.getItems().asSingleItems().getSchema();
                if (eleSchema.isValueTypeSchema()) {
                    child.setJsonType(calcValueDataType(eleSchema.asValueTypeSchema(), true));
                } else if (eleSchema.isObjectSchema()) {
                    parseFieldTypes(eleSchema.asObjectSchema(), true, child);
                } else {
                    // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                    return;
                }
            } else {
                // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                return;
            }

            child.setParentField(parent);
            children.add(child);
        });

        parent.setChildFields(children);
        return parent;
    }

    private FieldTypeEnum calcValueDataType(ValueTypeSchema vSchema, boolean isValueInArray) {
        if (vSchema.isNumberSchema()) {
            if (isValueInArray) {
                return FieldTypeEnum.numberArray;
            } else {
                return FieldTypeEnum.number;
            }
        } else if (vSchema.isStringSchema()) {
            if (isValueInArray) {
                return FieldTypeEnum.stringArray;
            } else {
                return FieldTypeEnum.string;
            }
        } else if (vSchema.isBooleanSchema()) {
            if (isValueInArray) {
                return FieldTypeEnum.booleanArray;
            } else {
                return FieldTypeEnum.bool;
            }
        } else {
            throw new IllegalArgumentException(vSchema.toString());
        }
    }

}
