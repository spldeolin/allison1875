package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeValidRetval;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.enums.JsonIntegerTypeEnum;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuilderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class JsgBuilderServiceImpl implements JsgBuilderService {

    @Override
    public JsonSchemaGenerator buildJsg(Table<String, String, AnalyzeFieldVarsRetval> afvRetvals,
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
                // 当开发者指定了Access.READ_ONLY时，如果是reqDTO，则算作ignore
                Access propertyAccess = super.findPropertyAccess(m);
                if (forReqOrResp && propertyAccess == Access.READ_ONLY) {
                    return true;
                }

                // 当开发者指定了Access.WRITE_ONLY时候，如果是respDTO，则算作ignore
                if (!forReqOrResp && propertyAccess == Access.WRITE_ONLY) {
                    return true;
                }

                // 拓展分析
                String className = m.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = m.getName();
                AnalyzeFieldVarsRetval afvRetval = afvRetvals.get(className, fieldNameMight);
                if (isIgnored(m, afvRetval, forReqOrResp)) {
                    return true;
                }

                return super.hasIgnoreMarker(m);
            }

            @Override
            public String findPropertyDescription(Annotated annotated) {
                Field field = findFieldEvenIfAnnotatedMethod(annotated.getAnnotated());
                if (field == null) {
                    return null;
                }

                String className = field.getDeclaringClass().getName().replace('$', '.');
                String fieldNameMight = field.getName();

                JsonPropertyDescriptionValueDTO jpdv = new JsonPropertyDescriptionValueDTO();

                // jpdv 注释
                AnalyzeFieldVarsRetval afvRetval = afvRetvals.get(className, fieldNameMight);
                if (afvRetval != null) {
                    jpdv.getCommentLines().addAll(afvRetval.getCommentLines());
                }

                // jpdv 兼容性
                if (afvRetval != null) {
                    jpdv.setDeprecatedDescription(afvRetval.getDeprecatedDescription());
                    jpdv.setSinceVersion(afvRetval.getSinceVersion());
                }

                // jpdv 枚举项
                if (afvRetval != null) {
                    jpdv.getAnalyzeEnumConstantsRetvals().addAll(afvRetval.getAnalyzeEnumConstantsRetvals());
                }

                // jpdv 校验项
                if (forReqOrResp) {
                    jpdv.getValids().addAll(analyzeValid(annotated.getAnnotated()));
                }
                /*
                    解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
                    e.g: private List<@NotBlank @Length(max = 10) String> userNames;
                 */
                boolean isLikeCollection = Collection.class.isAssignableFrom(annotated.getType().getRawClass());
                if (forReqOrResp && isLikeCollection) {
                    AnnotatedType at = field.getAnnotatedType();
                    if (at instanceof AnnotatedParameterizedType) {
                        AnnotatedType[] fieldTypeArguments =
                                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments();
                        if (fieldTypeArguments.length == 1) {
                            AnnotatedType collectionParamType = fieldTypeArguments[0];
                            List<AnalyzeValidRetval> collectonParamTypeValids = analyzeValid(collectionParamType);
                            collectonParamTypeValids.forEach(
                                    one -> one.setValidatorType("列表内元素" + one.getValidatorType()));
                            jpdv.getValids().addAll(collectonParamTypeValids);
                        }
                    }
                }

                // jpdv 格式
                Annotation jsonFormat = find(field, "com.fasterxml.jackson.annotation.JsonFormat");
                String formatPattern = "";
                if (jsonFormat != null) {
                    Object patternObj = invokeAnnoMethod(jsonFormat, "pattern");
                    if (patternObj instanceof String) {
                        formatPattern = (String) patternObj;
                    }
                }
                jpdv.setFormatPattern(formatPattern);

                // jpdv 更多分析后生成的文档
                if (afvRetval != null) {
                    jpdv.getMoreDocLines().addAll(afvRetval.getMoreDocLines());
                }

                // jpdv JSON整数细类
                if (field.getType() == Long.class || field.getType() == long.class) {
                    jpdv.setJsonIntegerTypeEnum(JsonIntegerTypeEnum.JAVA_LONG);
                } else if (field.getType() == Integer.class || field.getType() == int.class) {
                    jpdv.setJsonIntegerTypeEnum(JsonIntegerTypeEnum.JAVA_INTEGER);
                } else if (field.getType() == Short.class || field.getType() == short.class) {
                    jpdv.setJsonIntegerTypeEnum(JsonIntegerTypeEnum.JAVA_SHORT);
                } else if (field.getType() == Byte.class || field.getType() == byte.class) {
                    jpdv.setJsonIntegerTypeEnum(JsonIntegerTypeEnum.JAVA_BYTE);
                } else {
                    jpdv.setJsonIntegerTypeEnum(JsonIntegerTypeEnum.OTHERS);
                }

                return jpdv.serialize();
            }

            private Field findFieldEvenIfAnnotatedMethod(AnnotatedElement annotated) {
                if (annotated instanceof Field) {
                    return (Field) annotated;
                }
                if (annotated instanceof Method) {
                    Method method = (Method) annotated;
                    String fieldName;
                    if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
                        fieldName = MoreStringUtils.toLowerCamel(method.getName().substring(3));
                    } else if (method.getName().startsWith("is")) {
                        fieldName = MoreStringUtils.toLowerCamel(method.getName().substring(2));
                    } else {
                        fieldName = method.getName();
                    }
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
        return new JsonSchemaGenerator(customOm);
    }

    protected boolean isIgnored(AnnotatedMember m, @Nullable AnalyzeFieldVarsRetval afvRetval, boolean forReqOrResp) {
        if (afvRetval == null) {
            return false;
        }
        boolean isIngored = afvRetval.getCommentLines().stream().anyMatch(l -> l.trim().equals("#API-DOC-IGNORE#"));
        if (isIngored) {
            log.info("ignore {} clause #API-DOC-IGNORE# is found", m);
        }
        return isIngored;
    }

    protected List<AnalyzeValidRetval> analyzeValid(AnnotatedElement annotatedElement) {
        List<AnalyzeValidRetval> valids = Lists.newArrayList();
        if (find(annotatedElement, "javax.validation.constraints.NotNull") != null
                || find(annotatedElement, "jakarta.validation.constraints.NotNull") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_NULL.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.NotEmpty") != null
                || find(annotatedElement, "jakarta.validation.constraints.NotEmpty") != null
                || find(annotatedElement, "org.hibernate.validator.constraints.NotEmpty") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_EMPTY.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.NotBlank") != null
                || find(annotatedElement, "jakarta.validation.constraints.NotBlank") != null
                || find(annotatedElement, "org.hibernate.validator.constraints.NotBlank") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NOT_BLANK.getValue()));
        }

        // Size (javax + jakarta)
        Annotation size = find(annotatedElement, "javax.validation.constraints.Size");
        if (size == null) {
            size = find(annotatedElement, "jakarta.validation.constraints.Size");
        }
        if (size != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(size, "min"))));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(size, "max"))));
        }

        // Length (hibernate)
        Annotation length = find(annotatedElement, "org.hibernate.validator.constraints.Length");
        if (length != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_SIZE.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(length, "min"))));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_SIZE.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(length, "max"))));
        }

        // Min (javax + jakarta)
        Annotation min = find(annotatedElement, "javax.validation.constraints.Min");
        if (min == null) {
            min = find(annotatedElement, "jakarta.validation.constraints.Min");
        }
        if (min != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(min, "value"))));
        }

        // DecimalMin (javax + jakarta)
        Annotation decimalMin = find(annotatedElement, "javax.validation.constraints.DecimalMin");
        if (decimalMin == null) {
            decimalMin = find(annotatedElement, "jakarta.validation.constraints.DecimalMin");
        }
        if (decimalMin != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MIN_NUMBER.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(decimalMin, "value"))));
        }

        // Max (javax + jakarta)
        Annotation max = find(annotatedElement, "javax.validation.constraints.Max");
        if (max == null) {
            max = find(annotatedElement, "jakarta.validation.constraints.Max");
        }
        if (max != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(max, "value"))));
        }

        // DecimalMax (javax + jakarta)
        Annotation decimalMax = find(annotatedElement, "javax.validation.constraints.DecimalMax");
        if (decimalMax == null) {
            decimalMax = find(annotatedElement, "jakarta.validation.constraints.DecimalMax");
        }
        if (decimalMax != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_NUMBER.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(decimalMax, "value"))));
        }

        if (find(annotatedElement, "javax.validation.constraints.Future") != null
                || find(annotatedElement, "jakarta.validation.constraints.Future") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.FUTURE.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.FutureOrPresent") != null
                || find(annotatedElement, "jakarta.validation.constraints.FutureOrPresent") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.FUTURE_OR_PRESENT.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.Past") != null
                || find(annotatedElement, "jakarta.validation.constraints.Past") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.PAST.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.PastOrPresent") != null
                || find(annotatedElement, "jakarta.validation.constraints.PastOrPresent") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.PAST_OR_PRESENT.getValue()));
        }

        // Digits (javax + jakarta)
        Annotation digits = find(annotatedElement, "javax.validation.constraints.Digits");
        if (digits == null) {
            digits = find(annotatedElement, "jakarta.validation.constraints.Digits");
        }
        if (digits != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_INTEGRAL_DIGITS.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(digits, "integer"))));
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.MAX_FRACTIONAL_DIGITS.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(digits, "fraction"))));
        }

        if (find(annotatedElement, "javax.validation.constraints.Positive") != null
                || find(annotatedElement, "jakarta.validation.constraints.Positive") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.POSITIVE.getValue()));
        }

        if (find(annotatedElement, "javax.validation.constraints.Negative") != null
                || find(annotatedElement, "jakarta.validation.constraints.Negative") != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.NEGATIVE.getValue()));
        }

        // Pattern (javax + jakarta)
        Annotation pattern = find(annotatedElement, "javax.validation.constraints.Pattern");
        if (pattern == null) {
            pattern = find(annotatedElement, "jakarta.validation.constraints.Pattern");
        }
        if (pattern != null) {
            valids.add(new AnalyzeValidRetval().setValidatorType(ValidatorTypeEnum.REGEX.getValue())
                    .setNote(String.valueOf(invokeAnnoMethod(pattern, "regexp"))));
        }

        valids.forEach(valid -> {
            if (valid.getNote() == null) {
                valid.setNote("");
            }
        });
        return valids;
    }

    /**
     * 通过注解全限定名从目标项目的ClassLoader动态加载注解类，再用{@link AnnotatedElementUtils}查找注解实例
     *
     * <p>使用字符串全限定名而非硬编码{@code .class}引用，避免因Allison 1875自身的ClassLoader与目标项目的ClassLoader
     * 不同导致同名注解类的{@code Class}对象不相等的问题。
     *
     * @param element 被注解的元素（Field、Method等）
     * @param annotationClassName 注解的全限定名
     * @return 找到的注解实例，未找到或注解类不在目标项目classpath中时返回null
     */
    private Annotation find(AnnotatedElement element, String annotationClassName) {
        try {
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) AstForestContext.get()
                    .getClassLoader().loadClass(annotationClassName);
            return AnnotatedElementUtils.findMergedAnnotation(element, annotationType);
        } catch (ClassNotFoundException e) {
            // 目标项目classpath中没有该注解类，属于正常情况（如项目只用javax不用jakarta）
            return null;
        }
    }

    /**
     * 通过反射调用注解实例的属性方法
     */
    private Object invokeAnnoMethod(Annotation annotation, String methodName) {
        try {
            return annotation.annotationType().getMethod(methodName).invoke(annotation);
        } catch (Exception e) {
            log.warn("fail to invoke {}() on annotation {}", methodName, annotation.annotationType().getName(), e);
            return null;
        }
    }

}
