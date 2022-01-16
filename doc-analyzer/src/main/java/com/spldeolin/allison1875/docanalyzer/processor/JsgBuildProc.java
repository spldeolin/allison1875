package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.validation.constraints.AssertTrue;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.handle.AccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
public class JsgBuildProc {

    @Inject
    private SpecificFieldDescriptionsHandle specificFieldDescriptionsHandle;

    @Inject
    private AnalyzeEnumConstantHandle analyzeEnumConstantHandle;

    @Inject
    private MoreJpdvAnalysisHandle moreJpdvAnalysisHandle;

    @Inject
    private ValidProc validProc;

    @Inject
    private AccessDescriptionHandle accessDescriptionHandle;

    public JsonSchemaGenerator analyzeAstAndBuildJsg(@Nullable AstForest astForest) {
        Table<String, String, JsonPropertyDescriptionValueDto> jpdvs;
        if (astForest != null) {
            jpdvs = analyze(astForest);
            astForest.reset();
        } else {
            jpdvs = HashBasedTable.create();
        }

        // 缺省配置
        ObjectMapper customOm = JsonUtils.createObjectMapper();

        // 只有类属性可见，类的getter、setter、构造方法里的字段不会被当作JSON的字段
        customOm.setVisibility(customOm.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        customOm.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -3267511125040673149L;

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                String className = m.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = m.getName();
                JsonPropertyDescriptionValueDto jpdv = jpdvs.get(className, fieldNameMight);
                if (jpdv != null) {
                    Boolean docIgnore = jpdv.getDocIgnore();
                    if (docIgnore) {
                        return true;
                    } else {
                        // 没有doc-ignore仍需要考虑是否有@JsonIgnore注解
                        return super.hasIgnoreMarker(m);
                    }
                }

                return super.hasIgnoreMarker(m);
            }

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Field field = findFieldEvenIfAnnotatedMethod(annotated.getAnnotated());
                Collection<ValidatorDto> valids = validProc.process(annotated.getAnnotated());

                if (field == null) {
                    JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                    if (annotated.getAnnotated() instanceof Method
                            && annotated.getAnnotation(AssertTrue.class) != null) {
                        jpdv.setIsFieldCrossingValids(true);
                        jpdv.setValids(valids);
                    }
                    return JsonUtils.toJson(jpdv);
                }

                Class<?> clazz = field.getDeclaringClass();
                String className = clazz.getName().replace('$', '.');
                String fieldNameMight = field.getName();

                JsonPropertyDescriptionValueDto jpdv = jpdvs.get(className, fieldNameMight);
                if (jpdv == null) {
                    jpdv = new JsonPropertyDescriptionValueDto();
                    String specificDesc = specificFieldDescriptionsHandle.provideSpecificFieldDescriptions()
                            .get(className, fieldNameMight);
                    if (specificDesc != null) {
                        jpdv.setDescriptionLines(Lists.newArrayList(specificDesc));
                    }
                }

                jpdv.setValids(valids);

                /*
                    解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
                    e.g: private Collection<@NotBlank @Length(max = 10) String> userNames;
                 */
                boolean isLikeCollection = Collection.class.isAssignableFrom(annotated.getType().getRawClass());
                if (isLikeCollection) {
                    AnnotatedType at = field.getAnnotatedType();
                    if (at instanceof AnnotatedParameterizedType) {
                        AnnotatedType[] fieldTypeArguments =
                                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments();
                        if (fieldTypeArguments.length == 1) {
                            AnnotatedType theOnlyTypeArgument = fieldTypeArguments[0];
                            Collection<ValidatorDto> theOnlyElementValids = validProc.process(theOnlyTypeArgument);
                            theOnlyElementValids.forEach(one -> one.setValidatorType("列表内元素" + one.getValidatorType()));
                            jpdv.getValids().addAll(theOnlyElementValids);
                        }
                    }
                }

                JsonFormat jsonFormat = AnnotatedElementUtils.findMergedAnnotation(field, JsonFormat.class);
                if (jsonFormat != null) {
                    jpdv.setJsonFormatPattern(jsonFormat.pattern());
                }

                jpdv.setMore(moreJpdvAnalysisHandle.moreAnalysisFromField(field));

                return JsonUtils.toJson(jpdv);
            }

            private Field findFieldEvenIfAnnotatedMethod(AnnotatedElement annotated) {
                if (annotated instanceof Field) {
                    return (Field) annotated;
                }
                if (annotated instanceof Method) {
                    Method method = (Method) annotated;
                    String fieldName = MoreStringUtils.lowerFirstLetter(method.getName().substring(2));
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
                if (annoClass == JsonValue.class) {
                    if (annotated instanceof AnnotatedMember) {
                        Class<?> enumTypeMight = ((AnnotatedMember) annotated).getDeclaringClass();
                        if (enumTypeMight.isEnum() && analyzeEnumConstantHandle.supportEnumType(enumTypeMight)) {
                            return null;
                        }
                    }
                }
                return super._findAnnotation(annotated, annoClass);
            }

            @Override
            public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
                if (analyzeEnumConstantHandle.supportEnumType(enumType)) {
                    Object[] enumConstants = enumType.getEnumConstants();
                    Collection<String> ecat = Lists.newArrayList();
                    for (Object enumConstant : enumConstants) {
                        ecat.add(JsonUtils.toJson(analyzeEnumConstantHandle.analyzeEnumConstant(enumConstant)));
                    }
                    return ecat.toArray(new String[0]);
                }
                return super.findEnumValues(enumType, enumValues, names);
            }
        });

        JsonSchemaGenerator jsg = new JsonSchemaGenerator(customOm);

        return jsg;
    }

    private Table<String, String, JsonPropertyDescriptionValueDto> analyze(AstForest astForest) {
        Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = HashBasedTable.create();
        for (CompilationUnit cu : astForest) {
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                td.ifClassOrInterfaceDeclaration(coid -> collectPropertyDescriptions(coid, jpdvs));
            }
        }
        return jpdvs;
    }

    private void collectPropertyDescriptions(ClassOrInterfaceDeclaration coid,
            Table<String, String, JsonPropertyDescriptionValueDto> table) {
        String qualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        String javabeanQualifier = qualifier;
        for (FieldDeclaration field : coid.getFields()) {
            Collection<String> javadocDescLines = accessDescriptionHandle.accessField(field);
            for (VariableDeclarator var : field.getVariables()) {
                JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                String varName = var.getNameAsString();
                jpdv.setDescriptionLines(javadocDescLines);
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

}
