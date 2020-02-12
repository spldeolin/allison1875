package com.spldeolin.allison1875.da.core.processor;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoader;
import com.spldeolin.allison1875.base.util.Strings;
import com.spldeolin.allison1875.da.core.constant.QualifierConstants;
import com.spldeolin.allison1875.da.core.domain.UriFieldDomain;
import com.spldeolin.allison1875.da.core.enums.FieldType;
import com.spldeolin.allison1875.da.core.enums.NumberFormatType;
import com.spldeolin.allison1875.da.core.enums.StringFormatType;
import com.spldeolin.allison1875.da.core.util.ResolvedTypes;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-06
 */
@Log4j2
public class RequestParamProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    public Collection<UriFieldDomain> processor(Collection<Parameter> parameters) {
        Collection<UriFieldDomain> result = Lists.newLinkedList();
        for (Parameter parameter : parameters) {
            UriFieldDomain field = new UriFieldDomain();
            AnnotationExpr requestParam = parameter.getAnnotationByName("RequestParam").get();
            String name = null;
            boolean required = false;
            if (requestParam.isSingleMemberAnnotationExpr()) {
                name = requestParam.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
            }
            if (requestParam.isNormalAnnotationExpr()) {
                NormalAnnotationExpr normal = requestParam.asNormalAnnotationExpr();
                for (MemberValuePair pair : normal.getPairs()) {
                    String pairName = pair.getNameAsString();
                    if ("required".equals(pairName)) {
                        required = pair.getValue().asBooleanLiteralExpr().getValue();
                    }
                    if (StringUtils.equalsAny(pairName, "name", "value")) {
                        name = pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
            if (requestParam.isMarkerAnnotationExpr() || name == null) {
                name = parameter.getNameAsString();
            }
            field.fieldName(name).required(required);

            FieldType jsonType;
            NumberFormatType numberFormat = null;
            StringBuilder stringFormat = new StringBuilder();
            ResolvedType type = parameter.getType().resolve();
            String describe = type.describe();
            JsonSchema jsonSchema = generateSchema(describe);
            if (jsonSchema != null && jsonSchema.isValueTypeSchema()) {
                if (jsonSchema.isStringSchema()) {
                    jsonType = FieldType.string;
                    parameter.getAnnotationByClass(DateTimeFormat.class)
                            .ifPresent(dateTimeFormat -> dateTimeFormat.ifNormalAnnotationExpr(normal -> {
                                normal.getPairs().forEach(pair -> {
                                    if (pair.getNameAsString().equals("pattern")) {
                                        stringFormat.append(f(StringFormatType.datetime.getValue(), pair.getValue()));
                                    }
                                });
                            }));
                    if (stringFormat.length() == 0) {
                        stringFormat.append(StringFormatType.normal.getValue());
                    }

                } else if (jsonSchema.isNumberSchema()) {
                    jsonType = FieldType.number;

                    if (!jsonSchema.isIntegerSchema()) {
                        numberFormat = NumberFormatType.f1oat;
                    } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.INTEGER, "int")) {
                        numberFormat = NumberFormatType.int32;
                    } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.LONG, "long")) {
                        numberFormat = NumberFormatType.int64;
                    } else {
                        numberFormat = NumberFormatType.inT;
                    }
                } else if (jsonSchema.isBooleanSchema()) {
                    jsonType = FieldType.bool;
                } else {
                    throw new RuntimeException("impossible unless bug");
                }
            } else if (ResolvedTypes.isOrLike(type, QualifierConstants.MULTIPART_FILE)) {
                jsonType = FieldType.file;
            } else {
                log.warn("暂不支持解析ValueSchema以外的@RequestParam [{}]", parameter);
                continue;
            }
            field.jsonType(jsonType).numberFormat(numberFormat);

            field.validators(new ValidatorProcessor().process(parameter));
            result.add(field);
        }
        return result;
    }

    private JsonSchema generateSchema(String resolvedTypeDescribe) {
        JsonSchema jsonSchema = generateSchemaByQualifierForClassLoader(resolvedTypeDescribe);
        if (jsonSchema == null && resolvedTypeDescribe.contains(".")) {
            generateSchema(Strings.replaceLast(resolvedTypeDescribe, "\\.", "$"));
        }
        return jsonSchema;
    }

    private JsonSchema generateSchemaByQualifierForClassLoader(String qualifierForClassLoader) {
        JavaType javaType;
        try {
            javaType = new TypeFactory(null) {
                private static final long serialVersionUID = -8151903006798193420L;

                @Override
                public ClassLoader getClassLoader() {
                    return WarOrFatJarClassLoader.classLoader;
                }
            }.constructFromCanonical(qualifierForClassLoader);
        } catch (IllegalArgumentException e) {
            log.warn("TypeFactory.constructFromCanonical({})", qualifierForClassLoader);
            return null;
        }
        try {
            return jsg.generateSchema(javaType);
        } catch (JsonMappingException e) {
            log.warn("jsg.generateSchema({})", javaType);
            return null;
        }
    }

}
