package com.spldeolin.allison1875.da.core.processor;

import static com.spldeolin.allison1875.da.DocAnalyzerConfig.CONFIG;

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
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.Strings;
import com.spldeolin.allison1875.base.util.ast.ResolvedTypes;
import com.spldeolin.allison1875.da.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
@Accessors(fluent = true)
class BodyStructureProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    @Setter
    private ResolvedType bodyType;

    @Setter
    protected Boolean forRequestBodyOrNot;

    protected Boolean inArray = false;

    protected Boolean inPage = false;

    BodyStructureEnum calcBodyStructure() {
        throw new IllegalStateException("Cannot call this method before calling process method.");
    }

    BodyStructureProcessor moreProcess(ApiDefinition api) {
        throw new IllegalStateException("Cannot call this method before calling process method.");
    }

    BodyStructureProcessor process() {
        checkStatus();

        if (bodyType == null) {
            return new VoidBodyProcessor();
        }

        BodyStructureProcessor result;
        try {
            if (isArray(bodyType)) {
                // 最外层是 数组
                result = tryProcessNonArrayLikeType(getArrayElementType(bodyType));
                result.inArray = true;
            } else if (isJucAndElementTypeExplicit(bodyType)) {
                // 最外层是 列表
                result = tryProcessNonArrayLikeType(getJUCElementType(bodyType));
                result.inArray = true;
            } else if (isPage(bodyType)) {
                // 最外层是 Page对象
                result = tryProcessNonArrayLikeType(getPageElementType(bodyType));
                result.inPage = true;
            } else {
                // 单层
                result = tryProcessNonArrayLikeType(bodyType);
            }
        } catch (Exception e) {
            log.warn("type={}, cause={}", bodyType.describe(), e.getMessage());
            // as mazy mode
            result = new ChaosBodyProcessor().jsonSchema(generateSchema(bodyType.describe()));
        }

        // 新对象调用fillProcessResultToApi时需要用到forRequestBodyOrNot属性
        return result.forRequestBodyOrNot(forRequestBodyOrNot);
    }

    private void checkStatus() {
        if (forRequestBodyOrNot == null) {
            throw new IllegalStateException("forRequestBodyOrNot cannot be absent.");
        }
    }

    private BodyStructureProcessor tryProcessNonArrayLikeType(ResolvedType type) {
        String describe = type.describe();
        ClassOrInterfaceDeclaration coid = StaticAstContainer.getClassOrInterfaceDeclaration(describe);

        JsonSchema jsonSchema;
        if (coid == null) {
            // 往往是泛型返回值或是非用户定义的类型
            jsonSchema = generateSchema(describe);
        } else {
            jsonSchema = generateSchema(coid);
        }

        if (jsonSchema == null) {
            log.warn("Cannot generate json schema [{}]", describe);
            return new VoidBodyProcessor();

        } else if (jsonSchema.isObjectSchema()) {
            return new KeyValueBodyProcessor().objectSchema(jsonSchema.asObjectSchema());

        } else if (jsonSchema.isValueTypeSchema()) {
            FieldTypeEnum jsonType;
            NumberFormatTypeEnum numberFormat = null;
            if (jsonSchema.isStringSchema()) {
                jsonType = FieldTypeEnum.string;
            } else if (jsonSchema.isNumberSchema()) {
                jsonType = FieldTypeEnum.number;

                if (!jsonSchema.isIntegerSchema()) {
                    numberFormat = NumberFormatTypeEnum.f1oat;
                } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.INTEGER, "int")) {
                    numberFormat = NumberFormatTypeEnum.int32;
                } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.LONG, "long")) {
                    numberFormat = NumberFormatTypeEnum.int64;
                } else {
                    numberFormat = NumberFormatTypeEnum.inT;
                }
            } else if (jsonSchema.isBooleanSchema()) {
                jsonType = FieldTypeEnum.bool;
            } else {
                throw new RuntimeException("impossible unless bug");
            }
            return new ValueBodyProcessor().valueStructureJsonType(jsonType).valueStructureNumberFormat(numberFormat);

        } else {
            return new ChaosBodyProcessor().jsonSchema(jsonSchema);
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
            return type.asReferenceType().getId().equals(CONFIG.getCommonPageTypeQualifier());
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
        int typeParameterIndex = resolvedTypeDescribe.indexOf("<");
        if (typeParameterIndex != -1) {
            resolvedTypeDescribe = resolvedTypeDescribe.substring(0, typeParameterIndex);
        }
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
