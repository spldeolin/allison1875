package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.AssertTrue;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.AccessDescriptionService;
import com.spldeolin.allison1875.docanalyzer.service.AnalyzeEnumConstantService;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuildService;
import com.spldeolin.allison1875.docanalyzer.service.MoreJpdvAnalysisService;
import com.spldeolin.allison1875.docanalyzer.service.SpecificFieldDescriptionsService;
import com.spldeolin.allison1875.docanalyzer.service.ValidService;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
public class JsgBuildServiceImpl implements JsgBuildService {

    @Inject
    private SpecificFieldDescriptionsService specificFieldDescriptionsService;

    @Inject
    private AnalyzeEnumConstantService analyzeEnumConstantService;

    @Inject
    private MoreJpdvAnalysisService moreJpdvAnalysisService;

    @Inject
    private ValidService validService;

    @Inject
    private AccessDescriptionService accessDescriptionService;

    @Override
    public JsonSchemaGenerator analyzeAstAndBuildJsg(Table<String, String, JsonPropertyDescriptionValueDto> jpdvs,
            boolean forReqOrResp) {
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

                // 当开发者指定了doc-ignore时，算作ignore
                if (jpdv != null) {
                    if (jpdv.getDocIgnore()) {
                        return true;
                    }
                }

                // 当开发者指定了Access.READ_ONLY时，如果是reqDto，则算作ignore
                Access propertyAccess = super.findPropertyAccess(m);
                if (forReqOrResp && propertyAccess == Access.READ_ONLY) {
                    return true;
                }

                // 当开发者指定了Access.WRITE_ONLY时候，如果是respDto，则算作ignore
                if (!forReqOrResp && propertyAccess == Access.WRITE_ONLY) {
                    return true;
                }

                // 没有特殊情况，仍需要考虑是否有@JsonIgnore注解
                return super.hasIgnoreMarker(m);
            }

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Field field = findFieldEvenIfAnnotatedMethod(annotated.getAnnotated());
                List<ValidatorDto> valids;
                if (forReqOrResp) {
                    valids = validService.process(annotated.getAnnotated());
                } else {
                    valids = Lists.newArrayList();
                }

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
                    String specificDesc = specificFieldDescriptionsService.provideSpecificFieldDescriptions()
                            .get(className, fieldNameMight);
                    if (specificDesc != null) {
                        jpdv.setDescriptionLines(Lists.newArrayList(specificDesc));
                    }
                }

                jpdv.setAnnotatedName(annotated.toString());

                jpdv.setValids(valids);

                /*
                    解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
                    e.g: private Collection<@NotBlank @Length(max = 10) String> userNames;
                 */
                boolean isLikeCollection = Collection.class.isAssignableFrom(annotated.getType().getRawClass());
                if (forReqOrResp && isLikeCollection) {
                    AnnotatedType at = field.getAnnotatedType();
                    if (at instanceof AnnotatedParameterizedType) {
                        AnnotatedType[] fieldTypeArguments =
                                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments();
                        if (fieldTypeArguments.length == 1) {
                            AnnotatedType theOnlyTypeArgument = fieldTypeArguments[0];
                            Collection<ValidatorDto> theOnlyElementValids = validService.process(theOnlyTypeArgument);
                            theOnlyElementValids.forEach(
                                    one -> one.setValidatorType("列表内元素" + one.getValidatorType()));
                            jpdv.getValids().addAll(theOnlyElementValids);
                        }
                    }
                }

                JsonFormat jsonFormat = AnnotatedElementUtils.findMergedAnnotation(field, JsonFormat.class);
                jpdv.setJsonFormatPattern(Optional.ofNullable(jsonFormat).map(JsonFormat::pattern).orElse(""));

                jpdv.setMore(moreJpdvAnalysisService.moreAnalysisFromField(field));

                return JsonUtils.toJson(jpdv);
            }

            private Field findFieldEvenIfAnnotatedMethod(AnnotatedElement annotated) {
                if (annotated instanceof Field) {
                    return (Field) annotated;
                }
                if (annotated instanceof Method) {
                    Method method = (Method) annotated;
                    String fieldName = MoreStringUtils.lowerFirstLetter(method.getName().substring(3));
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
                        if (enumTypeMight.isEnum() && analyzeEnumConstantService.supportEnumType(enumTypeMight)) {
                            return null;
                        }
                    }
                }
                return super._findAnnotation(annotated, annoClass);
            }

            @Override
            public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
                if (analyzeEnumConstantService.supportEnumType(enumType)) {
                    Object[] enumConstants = enumType.getEnumConstants();
                    Collection<String> ecat = Lists.newArrayList();
                    for (Object enumConstant : enumConstants) {
                        ecat.add(JsonUtils.toJson(analyzeEnumConstantService.analyzeEnumConstant(enumConstant)));
                    }
                    return ecat.toArray(new String[0]);
                }
                return super.findEnumValues(enumType, enumValues, names);
            }
        });

        JsonSchemaGenerator jsg = new JsonSchemaGenerator(customOm);

        return jsg;
    }

    @Override
    public Table<String, String, JsonPropertyDescriptionValueDto> analyzeJpdvs(AstForest astForest) {
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
        /*
        这里不存在coid不应算作非法，而应忽略。
        因为coid可能是一个声明在class内部的class（比如handler-transformer转化前的block）
        这样的coid是符合Java语法的，会被扫描到的，但确实是没有qualifier的
         */
        coid.getFullyQualifiedName().ifPresent(qualifier -> {
            String javabeanQualifier = qualifier;
            for (FieldDeclaration field : coid.getFields()) {
                Collection<String> javadocDescLines = accessDescriptionService.accessField(field);
                for (VariableDeclarator var : field.getVariables()) {
                    JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                    String varName = var.getNameAsString();
                    jpdv.setDescriptionLines(javadocDescLines);
                    jpdv.setDocIgnore(findIgnoreFlag(javadocDescLines));
                    table.put(javabeanQualifier, varName, jpdv);
                }
            }
        });

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
