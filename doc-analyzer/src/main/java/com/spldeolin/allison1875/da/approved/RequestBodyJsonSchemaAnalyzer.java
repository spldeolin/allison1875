package com.spldeolin.allison1875.da.approved;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.classloader.ModuleClassLoaderFactory;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.da.approved.javabean.JavabeanProperty;
import com.spldeolin.allison1875.da.approved.javabean.JsonPropertyDescriptionValue;
import com.spldeolin.allison1875.da.deprecated.core.processor.ValidatorProcessor;
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

        Collection<JsonSchema> jsonSchemas = obtainJsonSchema(forest, jsg);


        return Lists.newArrayList();
    }

    private Collection<JsonSchema> obtainJsonSchema(AstForest forest, JsonSchemaGenerator jsg) {
        Collection<JsonSchema> result = Lists.newLinkedList();
        forest.reset();
        forest.forEach(cu -> {
            ClassLoader classLoader = ModuleClassLoaderFactory
                    .getClassLoader(cu.getStorage().orElseThrow(StorageAbsentException::new).getSourceRoot());
            cu.findAll(Parameter.class, parameter -> parameter.getAnnotationByName("RequestBody").isPresent())
                    .forEach(requestBody -> {
                        try {
                            String describe = requestBody.resolve().describeType();
                            log.info("describe={}", describe);
                            JsonSchema jsonSchema = JsonSchemaUtils.generateSchema(describe, classLoader, jsg);
                            result.add(jsonSchema);
                            log.info(JsonUtils.toJson(jsonSchema));
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
                if (Arrays.stream(enumType.getInterfaces())
                        .anyMatch(one -> one.getSimpleName().equals("IEnum"))) {
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
                                cad.setDescription(coidAndEnumInfos.get(enumType.getName().replace('$','.'),enumValues[i].toString()));
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
                    String className = annf.getDeclaringClass().getName().replace('$','.');
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
                    coidAndEnumInfos.put(qualifier,entry.getNameAsString(), Javadocs.extractFirstLine(entry));
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
                            value.setValidators(
                                    new ValidatorProcessor().nodeWithAnnotations(field).process().validators());

                            field.getVariables().forEach(var -> {
                                String variableName = var.getNameAsString();
                                value.setRawType(var.getTypeAsString());
                                coidAndEnumInfos.put(javabeanQualifier, variableName, JsonUtils.toJson(value));
                            });
                        });
                    });
        });
        return coidAndEnumInfos;
    }

    @Data
    public static class CodeAndDescription {

        private String code;

        private String description;

    }

}