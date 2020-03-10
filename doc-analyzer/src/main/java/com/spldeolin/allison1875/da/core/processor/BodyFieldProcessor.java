package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.exception.FieldAbsentException;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.da.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
import com.spldeolin.allison1875.da.core.enums.StringFormatTypeEnum;
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
        schema.getProperties().forEach((childFieldName, childSchema) -> {
            BodyFieldDefinition childFieldDto = new BodyFieldDefinition();
            String fieldVarQualifier =
                    StringUtils.removeStart(schema.getId(), "urn:jsonschema:").replace(':', '.') + "." + childFieldName;

            VariableDeclarator fieldVar = StaticAstContainer.getFieldVariableDeclarator(fieldVarQualifier);
            if (fieldVar == null) {
                /*
                被JsonSchema认为有这个field，但不存在field时，会出现这种fieldDeclaration=null的情况，目前已知的有：
                    类中有getMyField方法，但没有myField字段
                忽略它们即可
                 */
                return;
            }

            childFieldDto.setFieldName(childFieldName);



            if (childSchema.isValueTypeSchema()) {
                childFieldDto.setJsonType(calcValueDataType(childSchema.asValueTypeSchema(), false));
            } else if (childSchema.isObjectSchema()) {
                parseFieldTypes(childSchema.asObjectSchema(), false, childFieldDto);
            } else if (childSchema.isArraySchema()) {
                ArraySchema aSchema = childSchema.asArraySchema();
                if (aSchema.getItems() == null) {
                    log.warn("Cannot analyze the type of array element, field=[{}]", fieldVarQualifier);
                    return;
                }
                if (aSchema.getItems().isArrayItems()) {
                    log.warn("Cannot analyze the type of array element, field=[{}]", fieldVarQualifier);
                    return;
                }

                JsonSchema eleSchema = aSchema.getItems().asSingleItems().getSchema();
                if (eleSchema.isValueTypeSchema()) {
                    childFieldDto.setJsonType(calcValueDataType(eleSchema.asValueTypeSchema(), true));
                } else if (eleSchema.isObjectSchema()) {
                    parseFieldTypes(eleSchema.asObjectSchema(), true, childFieldDto);
                } else {
                    // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                    return;
                }
            } else {
                // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                return;
            }

            if (childFieldDto.getJsonType() == FieldTypeEnum.number) {
                String javaType = fieldVar.getTypeAsString();
                childFieldDto.setNumberFormat(calcNumberFormat(javaType));
            }

            if (childFieldDto.getJsonType() == FieldTypeEnum.string) {
                childFieldDto.setStringFormat(StringFormatTypeEnum.normal.getValue());
            }

            childFieldDto.setParentField(parent);
            children.add(childFieldDto);
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

    private NumberFormatTypeEnum calcNumberFormat(String javaTypeName) {
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Float", "Double", "BigDecimal")) {
            return NumberFormatTypeEnum.f1oat;
        } else if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Long", "BigInteger")) {
            return NumberFormatTypeEnum.int64;
        } else {
            return NumberFormatTypeEnum.int32;
        }
    }

}
