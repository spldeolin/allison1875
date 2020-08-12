package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.validation.constraints.AssertTrue;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeCustomValidationStrategy;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class JsgBuildProc {

    private final AstForest astForest;

    private final AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy;

    private final Table<String, String, String> specificFieldDescriptions;

    private final Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = HashBasedTable.create();

    public JsgBuildProc(AstForest astForest, AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy,
            Table<String, String, String> specificFieldDescriptions) {
        this.astForest = astForest;
        this.analyzeCustomValidationStrategy = analyzeCustomValidationStrategy;
        this.specificFieldDescriptions = specificFieldDescriptions;
    }

    public JsonSchemaGenerator analyzeAstAndBuildJsg() {
        analyze(astForest);
        return buildJsg();
    }

    private void analyze(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                td.ifClassOrInterfaceDeclaration(coid -> collectPropertyDescriptions(coid, jpdvs));
            }
        }
    }

    private void collectPropertyDescriptions(ClassOrInterfaceDeclaration coid,
            Table<String, String, JsonPropertyDescriptionValueDto> table) {
        String qualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        String javabeanQualifier = qualifier;
        for (FieldDeclaration field : coid.getFields()) {
            Collection<String> javadocDescLines = JavadocDescriptions.getEveryLine(field);
            for (VariableDeclarator var : field.getVariables()) {
                JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                String varName = var.getNameAsString();
                String description = specificFieldDescriptions.get(javabeanQualifier, varName);
                if (description == null) {
                    jpdv.setDescriptionLines(javadocDescLines);
                } else {
                    jpdv.setDescriptionLines(Lists.newArrayList(description));
                }
                jpdv.setDocIgnore(findIgnoreFlag(javadocDescLines));
                table.put(javabeanQualifier, varName, jpdv);
            }
        }
    }

    private boolean findIgnoreFlag(Collection<String> javadocDescLines) {
        for (String line : javadocDescLines) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, "doc-ignore")) {
                return true;
            }
        }
        return false;
    }

    public JsonSchemaGenerator buildJsg() {
        ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper());
        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -3267511125040673149L;

            private final Multiset<JavaType> count = HashMultiset.create();

            @Override
            public PropertyName findNameForSerialization(Annotated a) {
                if (a.getAnnotated() instanceof Method && a.getAnnotation(AssertTrue.class) != null) {
                    count.add(a.getType());
                    return PropertyName
                            .construct("跨字段校验项" + Strings.repeat(String.valueOf('\u0000'), count.count(a.getType())));
                }
                return super.findNameForSerialization(a);
            }

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                String className = m.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = m.getName();
                JsonPropertyDescriptionValueDto jpdv = jpdvs.get(className, fieldNameMight);
                if (jpdv != null) {
                    return jpdv.getDocIgnore();
                }

                return super.hasIgnoreMarker(m);
            }

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Field field = findFieldEvenIfAnnotatedMethod(annotated.getAnnotated());
                ValidProc validProc = new ValidProc(analyzeCustomValidationStrategy, annotated.getAnnotated())
                        .process();

                if (field == null) {
                    JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                    if (annotated.getAnnotated() instanceof Method
                            && annotated.getAnnotation(AssertTrue.class) != null) {
                        jpdv.setIsFieldCrossingValids(true);
                        jpdv.setValids(validProc.getValids());
                    }
                    return JsonUtils.toJson(jpdv);
                }

                Class<?> clazz = field.getDeclaringClass();
                String className = clazz.getName().replace('$', '.');
                String fieldNameMight = field.getName();

                JsonPropertyDescriptionValueDto jpdv = jpdvs.get(className, fieldNameMight);
                if (jpdv == null) {
                    jpdv = new JsonPropertyDescriptionValueDto();
                }

                jpdv.setValids(validProc.getValids());

                /*
                    解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
                    e.g: private Collection<@NotBlank @Length(max = 10) String> userNames;
                 */
                boolean isLikeCollection = annotated.getType().getRawClass().isAssignableFrom(Collection.class);
                if (isLikeCollection) {
                    AnnotatedType at = field.getAnnotatedType();
                    if (at instanceof AnnotatedParameterizedType) {
                        AnnotatedType[] fieldTypeArguments = ((AnnotatedParameterizedType) at)
                                .getAnnotatedActualTypeArguments();
                        if (fieldTypeArguments.length == 1) {
                            AnnotatedType theOnlyTypeArgument = fieldTypeArguments[0];
                            Collection<ValidatorDto> theOnlyElementValids = new ValidProc(
                                    analyzeCustomValidationStrategy, theOnlyTypeArgument).process().getValids();
                            theOnlyElementValids.forEach(one -> one.setValidatorType("内部元素" + one.getValidatorType()));
                            jpdv.getValids().addAll(theOnlyElementValids);
                        }
                    }
                }

                JsonFormat jsonFormat = AnnotatedElementUtils.findMergedAnnotation(field, JsonFormat.class);
                if (jsonFormat != null) {
                    jpdv.setJsonFormatPattern(jsonFormat.pattern());
                }

                return JsonUtils.toJson(jpdv);
            }

            private Field findFieldEvenIfAnnotatedMethod(AnnotatedElement annotated) {
                if (annotated instanceof Field) {
                    return (Field) annotated;
                }
                if (annotated instanceof Method) {
                    Method method = (Method) annotated;
                    String fieldName = StringUtils.lowerFirstLetter(method.getName().substring(2));
                    try {
                        return method.getDeclaringClass().getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
                if (annoClass == JsonSerialize.class) {
                    return null;
                }
                return super._findAnnotation(annotated, annoClass);
            }

        });

        JsonSchemaGenerator jsg = new JsonSchemaGenerator(
                // OM
                JsonUtils.initObjectMapper(om), new SchemaFactoryWrapper().setVisitorContext(new VisitorContext() {
            private final Multiset<JavaType> seenSchemasWithCount = HashMultiset.create();

            /**
             * 多个property是同一个Javabean时
             * 确保这些property的类型都是ObjectSchema
             * 而不是一个ObjectSchema外加其他的RefereceSchema
             */
            @Override
            public String addSeenSchemaUri(JavaType aSeenSchema) {
                if (aSeenSchema != null && !aSeenSchema.isPrimitive()) {
                    seenSchemasWithCount.add(aSeenSchema);
                    return javaTypeToUrn(aSeenSchema);
                }
                return null;
            }

            @Override
            public String getSeenSchemaUri(JavaType aSeenSchema) {
                int limit = 10; // 一般不会存在10个相同Type的Field，超过这个数字认为是递归了
                return (seenSchemasWithCount.count(aSeenSchema) > limit) ? javaTypeToUrn(aSeenSchema) : null;
            }

            @Override
            public String javaTypeToUrn(JavaType jt) {
                return jt.toCanonical();
            }
        }));

        return jsg;
    }

}
