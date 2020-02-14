package com.spldeolin.allison1875.da.core.processor;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.Config;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.Strings;
import com.spldeolin.allison1875.da.core.enums.FieldType;
import com.spldeolin.allison1875.da.core.enums.NumberFormatType;
import com.spldeolin.allison1875.da.core.processor.result.BodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.ChaosStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.KeyValueStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.ValueStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.VoidStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.util.ResolvedTypes;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@AllArgsConstructor
@Log4j2
public class BodyProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    private final ResolvedType type;

    public BodyProcessResult process() {
        if (type == null) {
            return new VoidStructureBodyProcessResult();
        }

        BodyProcessResult result;
        try {
            if (isArray(type)) {
                // 最外层是 数组
                result = tryProcessNonArrayLikeType(getArrayElementType(type)).inArray(true);
            } else if (isJucAndElementTypeExplicit(type)) {
                // 最外层是 列表
                result = tryProcessNonArrayLikeType(getJUCElementType(type)).inArray(true);
            } else if (isPage(type)) {
                // 最外层是 Page对象
                result = tryProcessNonArrayLikeType(getPageElementType(type)).inPage(true);
            } else {
                // 单层
                result = tryProcessNonArrayLikeType(type);
            }
        } catch (Exception e) {
            log.warn("type={}, cause={}", type.describe(), e.getMessage());
            // as mazy mode
            result = new ChaosStructureBodyProcessResult().jsonSchema(generateSchema(type.describe()));
        }
        return result;
    }

    private BodyProcessResult tryProcessNonArrayLikeType(ResolvedType type) {
        String describe = type.describe();
        ClassOrInterfaceDeclaration coid = StaticAstContainer.getClassOrInterfaceDeclaration(describe);
//                CoidContainer.getInstance().getByQualifier().get(describe);

        JsonSchema jsonSchema;
        if (coid == null) {
            // 往往是泛型返回值或是非用户定义的类型
            jsonSchema = generateSchema(describe);
        } else {
            jsonSchema = generateSchema(coid);
        }

        if (jsonSchema == null) {
            log.info("Cannot generate json schema [{}]", describe);
            return new VoidStructureBodyProcessResult();

        } else if (jsonSchema.isObjectSchema()) {
            return new KeyValueStructureBodyProcessResult().objectSchema(jsonSchema.asObjectSchema());

        } else if (jsonSchema.isValueTypeSchema()) {
            FieldType jsonType;
            NumberFormatType numberFormat = null;
            if (jsonSchema.isStringSchema()) {
                jsonType = FieldType.string;
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
            return new ValueStructureBodyProcessResult().valueStructureJsonType(jsonType)
                    .valueStructureNumberFormat(numberFormat);

        } else {
            return new ChaosStructureBodyProcessResult().jsonSchema(jsonSchema);
        }
    }

    private boolean isArray(ResolvedType type) {
        return type.isArray();
    }

    private ResolvedType getArrayElementType(ResolvedType arrayType) {
        return arrayType.asArrayType().getComponentType();
    }

    private boolean isJucAndElementTypeExplicit(ResolvedType type) {
        if (type.isReferenceType()) {
            ResolvedReferenceType referenceType = type.asReferenceType();
            // is J.U.C
            if (ResolvedTypes.isOrLike(referenceType, QualifierConstants.COLLECTION)) {
                // is element type explicit or not
                return referenceType.getTypeParametersMap().size() == 1;
            }
        }
        return false;
    }

    private ResolvedType getJUCElementType(ResolvedType JUCType) {
        return Iterables.getOnlyElement(JUCType.asReferenceType().getTypeParametersMap()).b;
    }

    private boolean isPage(ResolvedType type) {
        if (type.isReferenceType()) {
            return type.asReferenceType().getId().equals(Config.getCommonPageTypeQualifier());
        }
        return false;
    }

    private ResolvedType getPageElementType(ResolvedType pageType) {
        return Iterables.getOnlyElement(pageType.asReferenceType().getTypeParametersMap()).b;
    }

    private JsonSchema generateSchema(ClassOrInterfaceDeclaration clazz) {
        String qualifierForClassLoader = qualifierForClassLoader(clazz);
        return generateSchemaByQualifierForClassLoader(qualifierForClassLoader);
    }

    private JsonSchema generateSchema(String resolvedTypeDescribe) {
        JsonSchema jsonSchema = generateSchemaByQualifierForClassLoader(resolvedTypeDescribe);
        if (jsonSchema == null && resolvedTypeDescribe.contains(".")) {
            generateSchema(Strings.replaceLast(resolvedTypeDescribe, ".", "$"));
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
                    return WarOrFatJarClassLoaderFactory.getClassLoader();
                }
            }.constructFromCanonical(qualifierForClassLoader);
        } catch (IllegalArgumentException e) {
            return null;
        }
        try {
            return jsg.generateSchema(javaType);
        } catch (JsonMappingException e) {
            log.warn("jsg.generateSchema({})", javaType);
            return null;
        }
    }

    private String qualifierForClassLoader(ClassOrInterfaceDeclaration coid) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        this.qualifierForClassLoader(qualifierForClassLoader, coid);
        return qualifierForClassLoader.toString();
    }

    private void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                this.qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });
    }

}
