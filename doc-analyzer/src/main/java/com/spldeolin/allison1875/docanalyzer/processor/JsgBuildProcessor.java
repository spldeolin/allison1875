package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.dto.EnumDto;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeCustomValidationStrategy;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class JsgBuildProcessor {

    private final AstForest astForest;

    private final AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy;

    private final Table<String, String, String> extraFieldDescriptions;

    private final Table<String, String, String> enumDescriptions = HashBasedTable.create();

    private final Table<String, String, JsonPropertyDescriptionValueDto> propertyJpdvs = HashBasedTable.create();

    public JsgBuildProcessor(AstForest astForest, AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy,
            Table<String, String, String> extraFieldDescriptions) {
        this.astForest = astForest;
        this.analyzeCustomValidationStrategy = analyzeCustomValidationStrategy;
        this.extraFieldDescriptions = extraFieldDescriptions;
    }

    public JsonSchemaGenerator analyzeAstAndBuildJsg() {
        analyze(astForest);
        return buildJsg();
    }

    private void analyze(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                td.ifEnumDeclaration(ed -> collectEnumDescription(ed, enumDescriptions));
                td.ifClassOrInterfaceDeclaration(coid -> collectPropertiesAnnoInfo(coid, propertyJpdvs));
            }
        }
    }

    private void collectEnumDescription(EnumDeclaration ed, Table<String, String, String> table) {
        String qualifier = ed.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        ed.getEntries().forEach(entry -> {
            String comment = StringUtils.limitLength(JavadocDescriptions.getEveryLineInOne(entry, "，"), 4096);
            table.put(qualifier, entry.getNameAsString(), comment);
        });
    }

    private void collectPropertiesAnnoInfo(ClassOrInterfaceDeclaration coid,
            Table<String, String, JsonPropertyDescriptionValueDto> table) {
        String qualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        String javabeanQualifier = qualifier;
        for (FieldDeclaration field : coid.getFields()) {
            JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
            jpdv.setDescription(StringUtils.limitLength(JavadocDescriptions.getEveryLineInOne(field, "，"), 4096));
            for (VariableDeclarator var : field.getVariables()) {
                String variableName = var.getNameAsString();
                try {
                    jpdv.setRawType(var.getTypeAsString());
                } catch (Exception ignored) {
                }
                table.put(javabeanQualifier, variableName, jpdv);

                var.getType().ifPrimitiveType(pt -> {
                    if (pt.getType().name().equals("boolean")) {
                        table.put(javabeanQualifier, CodeGenerationUtils.getterName(boolean.class, variableName), jpdv);
                    }
                });
            }
        }

        ValidatorProcessor validatorProcessor = new ValidatorProcessor(analyzeCustomValidationStrategy);
        try {
            Class<?> aClass = LoadClassUtils.loadClass(qualifier, this.getClass().getClassLoader());
            Map<String, JsonPropertyDescriptionValueDto> row = table.row(qualifier);
            for (Field reflectionField : aClass.getDeclaredFields()) {
                JsonPropertyDescriptionValueDto jpdv = row.get(reflectionField.getName());
                if (jpdv != null) {
                    jpdv.setValidators(validatorProcessor.process(reflectionField));
                    JsonFormat jsonFormat = AnnotatedElementUtils
                            .findMergedAnnotation(reflectionField, JsonFormat.class);
                    if (jsonFormat != null) {
                        jpdv.setJsonFormatPattern(jsonFormat.pattern());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("类[{}]无法被加载", qualifier);
        }
    }

    public JsonSchemaGenerator buildJsg() {
        ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper());

        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -3267511125040673149L;

            private Field getFirstPropertyField(Class<?> enumType) {
                for (Field declaredField : enumType.getDeclaredFields()) {
                    if (declaredField.getType() != enumType) {
                        return declaredField;
                    }
                }
                return null;
            }

            @Override
            public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
                String[] result = new String[enumValues.length];

                Field codeField = getFirstPropertyField(enumType);
                if (codeField == null) {
                    // has no field any more.
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
                        String code = codeField.get(enumValues[i]).toString();
                        EnumDto cad = new EnumDto();
                        cad.setCode(code);
                        if (descriptionField != null) {
                            cad.setMeaning(descriptionField.get(enumValues[i]).toString());
                        } else {
                            cad.setMeaning(
                                    enumDescriptions.get(enumType.getName().replace('$', '.'), enumValues[i].name()));
                        }
                        result[i] = JsonUtils.toJson(cad);
                    } catch (IllegalAccessException e) {
                        // impossible unless bug
                        return super.findEnumValues(enumType, enumValues, names);
                    }
                }
                return result;
            }

            @Override
            public String findPropertyDescription(Annotated ann) {
                Class<?> clazz;
                String className;
                String fieldName = null;
                if (ann instanceof AnnotatedField) {
                    clazz = ((AnnotatedField) ann).getDeclaringClass();
                    fieldName = ann.getName();
                } else if (ann instanceof AnnotatedMethod) {
                    clazz = ((AnnotatedMethod) ann).getDeclaringClass();
                    if (ann.getName().startsWith("is")) {
                        fieldName = StringUtils.lowerFirstLetter(ann.getName().substring(2));
                    }
                } else {
                    return "{}";
                }
                className = clazz.getName().replace('$', '.');

                String extraDescription = extraFieldDescriptions.get(className, fieldName);
                JsonPropertyDescriptionValueDto jpdv = propertyJpdvs.get(className, fieldName);

                if (jpdv == null) {
                    jpdv = new JsonPropertyDescriptionValueDto();
                    if (extraDescription != null) {
                        jpdv.setDescription(extraDescription);
                    }
                    jpdv.setValidators(Lists.newArrayList());
                    jpdv.setRawType(ann.getRawType().getSimpleName());
                } else {
                    if (extraDescription != null) {
                        jpdv.setDescription(extraDescription);
                    }
                }
                return JsonUtils.toJson(jpdv);
            }

            @Override
            protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
                if (annoClass == JsonSerialize.class) {
                    return null;
                }
                return super._findAnnotation(annotated, annoClass);
            }

        });

        return new JsonSchemaGenerator(om);
    }

}
