package com.spldeolin.allison1875.da.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.da.ValidatorProcessor;
import com.spldeolin.allison1875.da.dto.EnumDto;
import com.spldeolin.allison1875.da.dto.JsonPropertyDescriptionValueDto;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的逻辑
 *
 * @author Deolin 2020-06-10
 */
public class JsonSchemaGeneratorProcessor {

    private final AstForest astForest;

    private final Table<String, String, String> enumDescriptions = HashBasedTable.create();

    private final Table<String, String, String> propertyJpdvs = HashBasedTable.create();

    public JsonSchemaGeneratorProcessor(AstForest astForest) {
        this.astForest = astForest;
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
            String comment = StringUtils.limitLength(Javadocs.extractFirstLine(entry), 4096);
            table.put(qualifier, entry.getNameAsString(), comment);
        });
    }

    private void collectPropertiesAnnoInfo(ClassOrInterfaceDeclaration coid, Table<String, String, String> table) {
        String javabeanQualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        for (FieldDeclaration field : coid.getFields()) {
            JsonPropertyDescriptionValueDto value = new JsonPropertyDescriptionValueDto();
            value.setComment(StringUtils.limitLength(Javadocs.extractFirstLine(field), 4096));
            value.setRequired(
                    Annotations.isAnnoPresent(field, NotNull.class) || Annotations.isAnnoPresent(field, NotEmpty.class)
                            || Annotations.isAnnoPresent(field, NotBlank.class));
            value.setValidators(new ValidatorProcessor().process(field));

            AnnotationExpr anno = Annotations.findAnno(field, JsonFormat.class);
            if (anno != null) {
                for (MemberValuePair pair : anno.asNormalAnnotationExpr().getPairs()) {
                    if (pair.getNameAsString().equals("pattern")) {
                        value.setJsonFormatPattern(pair.getValue().asStringLiteralExpr().getValue());
                    }
                }
            }

            for (VariableDeclarator var : field.getVariables()) {
                String variableName = var.getNameAsString();
                try {
                    value.setRawType(var.getTypeAsString());
                } catch (Exception ignored) {
                }
                String json = JsonUtils.toJson(value);
                table.put(javabeanQualifier, variableName, json);

                var.getType().ifPrimitiveType(pt -> {
                    if (pt.getType().name().equals("boolean")) {
                        table.put(javabeanQualifier, CodeGenerationUtils.getterName(boolean.class, variableName), json);
                    }
                });
            }
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
                if (ann instanceof AnnotatedField) {
                    clazz = ((AnnotatedField) ann).getDeclaringClass();
                } else if (ann instanceof AnnotatedMethod) {
                    clazz = ((AnnotatedMethod) ann).getDeclaringClass();
                } else {
                    return "{}";
                }

                String className = clazz.getName().replace('$', '.');
                String fieldName = ann.getName();
                String result = propertyJpdvs.get(className, fieldName);
                return result;
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
