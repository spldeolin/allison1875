package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
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

    private final ValidatorProcessor validatorProcessor;

    private final Table<String, String, String> specificFieldDescriptions;

    private final Table<String, String, String> enumDescriptions = HashBasedTable.create();

    private final Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = HashBasedTable.create();

    public JsgBuildProcessor(AstForest astForest, AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy,
            Table<String, String, String> specificFieldDescriptions) {
        this.astForest = astForest;
        this.validatorProcessor = new ValidatorProcessor(analyzeCustomValidationStrategy);
        this.specificFieldDescriptions = specificFieldDescriptions;
    }

    public JsonSchemaGenerator analyzeAstAndBuildJsg() {
        analyze(astForest);
        return buildJsg();
    }

    private void analyze(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                td.ifEnumDeclaration(ed -> collectEnumDescription(ed, enumDescriptions));
                td.ifClassOrInterfaceDeclaration(coid -> collectPropertyDescriptions(coid, jpdvs));
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

    private void collectPropertyDescriptions(ClassOrInterfaceDeclaration coid,
            Table<String, String, JsonPropertyDescriptionValueDto> table) {
        String qualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        String javabeanQualifier = qualifier;
        for (FieldDeclaration field : coid.getFields()) {
            for (VariableDeclarator var : field.getVariables()) {
                JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                String varName = var.getNameAsString();
                String description = specificFieldDescriptions.get(javabeanQualifier, varName);
                if (description == null) {
                    description = StringUtils.limitLength(JavadocDescriptions.getEveryLineInOne(field, "，"), 4096);
                }
                jpdv.setDescription(description);
                table.put(javabeanQualifier, varName, jpdv);
            }
        }
    }

    public JsonSchemaGenerator buildJsg() {
        ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper());
        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -3267511125040673149L;

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Class<?> clazz = getDeclaringClass(annotated);
                if (clazz == null) {
                    return JsonUtils.toJson(new JsonPropertyDescriptionValueDto());
                }
                String className = clazz.getName().replace('$', '.');
                String fieldName = getFieldName(annotated);

                JsonPropertyDescriptionValueDto jpdv = jpdvs.get(className, fieldName);
                if (jpdv == null) {
                    jpdv = new JsonPropertyDescriptionValueDto().setValidators(Lists.newArrayList());
                }

                jpdv.setValidators(validatorProcessor.process(annotated.getAnnotated()));

                if (annotated instanceof AnnotatedParameterizedType) {
                    AnnotatedType[] fieldTypeArguments = ((AnnotatedParameterizedType) annotated)
                            .getAnnotatedActualTypeArguments();
                    if (fieldTypeArguments.length == 1) {
                        AnnotatedType theOnlyTypeArgument = fieldTypeArguments[0];
                        jpdv.setTheOnlyTypeArgumentValidators(validatorProcessor.process(theOnlyTypeArgument));
                    }
                }

                JsonFormat jsonFormat = AnnotatedElementUtils.findMergedAnnotation(clazz, JsonFormat.class);
                if (jsonFormat != null) {
                    jpdv.setJsonFormatPattern(jsonFormat.pattern());
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

            private Class<?> getDeclaringClass(Annotated ann) {
                if (ann instanceof AnnotatedField) {
                    return ((AnnotatedField) ann).getDeclaringClass();
                }
                if (ann instanceof AnnotatedMethod) {
                    return ((AnnotatedMethod) ann).getDeclaringClass();
                }
                return null;
            }

            private String getFieldName(Annotated ann) {
                if (ann instanceof AnnotatedField) {
                    return ann.getName();
                }
                if (ann instanceof AnnotatedMethod) {
                    return StringUtils.lowerFirstLetter(ann.getName().substring(2));
                }
                return null;
            }

//            @Override
//            public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
//                String[] result = new String[enumValues.length];
//
//                Field codeField = getFirstPropertyField(enumType);
//                if (codeField == null) {
//                    // has no field any more.
//                    return super.findEnumValues(enumType, enumValues, names);
//                }
//
//                Field descriptionField = null;
//                try {
//                    descriptionField = enumType.getDeclaredField("description");
//                    descriptionField.setAccessible(true);
//                } catch (NoSuchFieldException e) {
//                    try {
//                        descriptionField = enumType.getDeclaredField("desc");
//                        descriptionField.setAccessible(true);
//                    } catch (NoSuchFieldException ignore) {
//                        // just enough
//                    }
//                }
//
//                codeField.setAccessible(true);
//                for (int i = 0; i < enumValues.length; i++) {
//                    try {
//                        String code = codeField.get(enumValues[i]).toString();
//                        EnumDto cad = new EnumDto();
//                        cad.setCode(code);
//                        if (descriptionField != null) {
//                            cad.setMeaning(descriptionField.get(enumValues[i]).toString());
//                        } else {
//                            cad.setMeaning(
//                                    enumDescriptions.get(enumType.getName().replace('$', '.'), enumValues[i].name()));
//                        }
//                        result[i] = JsonUtils.toJson(cad);
//                    } catch (IllegalAccessException e) {
//                        // impossible unless bug
//                        return super.findEnumValues(enumType, enumValues, names);
//                    }
//                }
//                return result;
//            }
//            private Field getFirstPropertyField(Class<?> enumType) {
//                for (Field declaredField : enumType.getDeclaredFields()) {
//                    if (declaredField.getType() != enumType) {
//                        return declaredField;
//                    }
//                }
//                return null;
//            }

        });

        JsonSchemaGenerator jsg = new JsonSchemaGenerator(om, JsonSchemaUtils.DEFAULT_SCHEMA_FACTORY_WRAPPER);

        return jsg;
    }

}
