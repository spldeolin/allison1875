package com.spldeolin.allison1875.da.approved;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.classloader.ModuleClassLoaderFactory;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.da.approved.enums.JsonFormatEnum;
import com.spldeolin.allison1875.da.approved.enums.JsonTypeEnum;
import com.spldeolin.allison1875.da.approved.javabean.JavabeanProperty;
import com.spldeolin.allison1875.da.approved.javabean.JsonPropertyDescriptionValue;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-04-25
 */
@Log4j2
public class RequestBodyJsonSchemaAnalyzer {

    public static void main(String[] args) {
        new RequestBodyJsonSchemaAnalyzer().analyze();
    }

    private Collection<JavabeanProperty> analyze() {
        AstForest forest = AstForest.getInstance();

        Table<String, String, String> coidAndEnumInfos = obtainCoidAndEnumInfos(forest);

        JsonSchemaGenerator jsg = buildJsg(coidAndEnumInfos);

        Map<String, JsonSchema> jsonSchemas = obtainJsonSchema(forest, jsg);

        Multimap<String, JavabeanProperty> javabeanProperties = ArrayListMultimap.create();
        for (Entry<String, JsonSchema> entry : jsonSchemas.entrySet()) {
            JsonSchema jsonSchema = entry.getValue();
            if (jsonSchema.isObjectSchema()) {
                JavabeanProperty tempParent = new JavabeanProperty();
                calcObjectTypeWithRecur(tempParent, jsonSchema.asObjectSchema(), false);
                javabeanProperties.putAll(entry.getKey(),
                        tempParent.getChildren().stream().map(child -> child.setParent(null))
                                .collect(Collectors.toList()));
            }
        }


        return Lists.newArrayList();
    }

    private JsonTypeEnum calcObjectTypeWithRecur(JavabeanProperty parent, ObjectSchema parentSchema,
            boolean isInArray) {
        parent.setJsonType(isInArray ? JsonTypeEnum.OBJECT_ARRAY : JsonTypeEnum.OBJECT);

        Collection<JavabeanProperty> children = Lists.newLinkedList();
        for (Entry<String, JsonSchema> entry : parentSchema.getProperties().entrySet()) {
            String childName = entry.getKey();
            JsonSchema childSchema = entry.getValue();
            JavabeanProperty child = new JavabeanProperty();
            child.setName(childName);
            child.setRawJsonSchema(childSchema);
            log.info("{}.{}", JsonSchemaUtils.getId(parentSchema), childName);

            if (childSchema.getDescription() != null) {
                JsonPropertyDescriptionValue jpdv = JsonUtils
                        .toObject(childSchema.getDescription(), JsonPropertyDescriptionValue.class);
                child.setDescription(jpdv.getComment());
                child.setValidators(jpdv.getValidators());
                child.setNullable(jpdv.getNullable());
                child.setJsonFormat(calcJsonFormat(jpdv.getRawType(), childSchema));
            } else {
                child.setJsonFormat(calcJsonFormat(null, childSchema));
                log.warn("Cannot found JsonPropertyDescriptionValue. {}.{}", JsonSchemaUtils.getId(parentSchema),
                        childName);
            }
            JsonTypeEnum jsonType;
            if (childSchema.isValueTypeSchema()) {
                jsonType = calcValueType(childSchema.asValueTypeSchema(), false);
            } else if (childSchema.isObjectSchema()) {
                jsonType = calcObjectTypeWithRecur(child, childSchema.asObjectSchema(), false);
            } else if (childSchema.isArraySchema()) {
                Items items = childSchema.asArraySchema().getItems();
                if (items == null || items.isArrayItems()) {
                    jsonType = JsonTypeEnum.UNKNOWN;
                } else {
                    JsonSchema eleSchema = items.asSingleItems().getSchema();
                    if (eleSchema.isValueTypeSchema()) {
                        jsonType = calcValueType(eleSchema.asValueTypeSchema(), true);
                    } else if (eleSchema.isObjectSchema()) {
                        jsonType = calcObjectTypeWithRecur(child, eleSchema.asObjectSchema(), true);
                    } else {
                        jsonType = JsonTypeEnum.UNKNOWN;
                    }
                }
            } else {
                jsonType = JsonTypeEnum.UNKNOWN;
            }
            child.setJsonType(jsonType);
            child.setParent(parent);
            children.add(child);
        }

        parent.setChildren(children);
        return parent.getJsonType();
    }

    private String calcJsonFormat(String rawType, JsonSchema childSchema) {
        if (childSchema.isValueTypeSchema() && !CollectionUtils.isEmpty(childSchema.asValueTypeSchema().getEnums())) {
            StringBuilder sb = new StringBuilder(64);
            for (String cadJson : childSchema.asStringSchema().getEnums()) {
                CodeAndDescription cad = JsonUtils.toObject(cadJson, CodeAndDescription.class);
                sb.append(cad.getCode()).append("-").append(cad.getDescription());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return String.format(JsonFormatEnum.ENUM.getValue(), sb);
        }

        if (childSchema.isNumberSchema() && rawType != null) {
            if (!childSchema.isIntegerSchema()) {
                return JsonFormatEnum.FLOAT.getValue();
            } else if (StringUtils.equalsAny(rawType, "Integer", "int")) {
                return JsonFormatEnum.INT_32.getValue();
            } else if (StringUtils.equalsAny(rawType, "Long", "long")) {
                return JsonFormatEnum.INT_64.getValue();
            } else {
                return JsonFormatEnum.INT_UNKNOWN.getValue();
            }
        }

        return JsonFormatEnum.NOTHING_SPECIAL.getValue();
    }

    private JsonTypeEnum calcValueType(ValueTypeSchema vSchema, boolean isInArray) {
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

    private Map<String, JsonSchema> obtainJsonSchema(AstForest forest, JsonSchemaGenerator jsg) {
        Map<String, JsonSchema> result = Maps.newHashMap();
        forest.reset();
        forest.forEach(cu -> {
            ClassLoader classLoader = ModuleClassLoaderFactory
                    .getClassLoader(cu.getStorage().orElseThrow(StorageAbsentException::new).getSourceRoot());
            cu.findAll(Parameter.class, parameter -> parameter.getAnnotationByName("RequestBody").isPresent())
                    .forEach(requestBody -> {
                        try {
                            String describe = requestBody.resolve().describeType();
//                            log.info("describe={}", describe);
                            JsonSchema jsonSchema = JsonSchemaUtils.generateSchema(describe, classLoader, jsg);
                            result.put(describe, jsonSchema);
//                            log.info(JsonUtils.toJson(jsonSchema));
                        } catch (Exception e) {
                            log.error(Locations.getRelativePathWithLineNo(requestBody), e);
                        }
                    });
        });
        return result;
    }

    private JsonSchemaGenerator buildJsg(Table<String, String, String> coidAndEnumInfos) {
        ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper());
        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -6279240404102583448L;

            @Override
            public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
                if (Arrays.stream(enumType.getInterfaces()).anyMatch(one -> one.getSimpleName().equals("IEnum"))) {
                    String[] result = new String[enumValues.length];
                    Field codeField;
                    try {
                        codeField = enumType.getDeclaredField("code");
                    } catch (NoSuchFieldException e) {
                        // impossible unless IEnum changed.
                        return super.findEnumValues(enumType, enumValues, names);
                    }

                    Field descriptionField = null;
                    try {
                        descriptionField = enumType.getDeclaredField("description");
                        descriptionField.setAccessible(true);
                    } catch (NoSuchFieldException e) {
                        try {
                            descriptionField = enumType.getDeclaredField("desc");
                            descriptionField.setAccessible(true);
                        } catch (NoSuchFieldException ignore) {
                            // just enough
                        }
                    }

                    codeField.setAccessible(true);
                    for (int i = 0; i < enumValues.length; i++) {
                        try {
                            String code = (String) codeField.get(enumValues[i]);
                            CodeAndDescription cad = new CodeAndDescription();
                            cad.setCode(code);
                            if (descriptionField != null) {
                                cad.setDescription((String) descriptionField.get(enumValues[i]));
                            } else {
                                cad.setDescription(coidAndEnumInfos
                                        .get(enumType.getName().replace('$', '.'), enumValues[i].toString()));
                            }
                            result[i] = JsonUtils.toJson(cad);
                        } catch (IllegalAccessException e) {
                            // impossible unless bug
                            return super.findEnumValues(enumType, enumValues, names);
                        }
                    }
                    return result;
                }
                return super.findEnumValues(enumType, enumValues, names);
            }

            @Override
            public String findPropertyDescription(Annotated ann) {
                String superResult = super.findPropertyDescription(ann);
                if (ann instanceof AnnotatedField && superResult == null) {
                    AnnotatedField annf = (AnnotatedField) ann;
                    String className = annf.getDeclaringClass().getName().replace('$', '.');
                    String fieldName = annf.getName();
                    String result = coidAndEnumInfos.get(className, fieldName);
                    return result;
                }
                return superResult;
            }
        });
        return new JsonSchemaGenerator(om);
    }

    private Table<String, String, String> obtainCoidAndEnumInfos(AstForest forest) {
        Table<String, String, String> coidAndEnumInfos = HashBasedTable.create();
        forest.forEach(cu -> {
            cu.findAll(EnumDeclaration.class).forEach(ed -> {
                String qualifier = ed.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
                ed.getEntries().forEach(entry -> {
                    coidAndEnumInfos.put(qualifier, entry.getNameAsString(), Javadocs.extractFirstLine(entry));
                });
            });
            cu.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getAnnotationByName("Data").isPresent())
                    .forEach(javabean -> {
                        String javabeanQualifier = javabean.getFullyQualifiedName()
                                .orElseThrow(QualifierAbsentException::new);
                        javabean.getFields().forEach(field -> {
                            JsonPropertyDescriptionValue value = new JsonPropertyDescriptionValue();
                            value.setComment(Javadocs.extractFirstLine(field));
                            value.setNullable(!field.getAnnotationByName("NotNull").isPresent() && !field
                                    .getAnnotationByName("NotEmpty").isPresent() && !field
                                    .getAnnotationByName("NotBlank").isPresent());
                            value.setValidators(new ValidatorProcessor().process(field));

                            field.getVariables().forEach(var -> {
                                String variableName = var.getNameAsString();
                                try {
                                    value.setRawType(var.getTypeAsString());
                                } catch (Exception ignored) {
                                }
                                coidAndEnumInfos.put(javabeanQualifier, variableName, JsonUtils.toJson(value));
                            });
                        });
                    });
        });
        return coidAndEnumInfos;
    }

    @Data
    private static class CodeAndDescription {

        private String code;

        private String description;

    }

}