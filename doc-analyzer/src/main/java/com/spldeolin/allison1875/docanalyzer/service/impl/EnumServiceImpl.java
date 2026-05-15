package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.service.EnumService;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-26
 */
@Singleton
@Slf4j
public class EnumServiceImpl implements EnumService {

    @Inject
    private Config config;

    @Override
    public List<AnalyzeEnumConstantsRetval> analyzeEnumConstants(VariableDeclarator fieldVar) {
        ResolvedType resolvedType;
        try {
            resolvedType = fieldVar.getType().resolve();
        } catch (Exception e) {
            log.info("Type of FieldVar '{}' cannot resolve", fieldVar);
            return Lists.newArrayList();
        }

        return analyzeEnumConstants(resolvedType);
    }

    private List<AnalyzeEnumConstantsRetval> analyzeEnumConstants(ResolvedType resolvedType) {
        if (resolvedType.isArray()) {
            ResolvedArrayType arrayType = resolvedType.asArrayType();
            return this.analyzeEnumConstants(arrayType.getComponentType());
        }

        if (!resolvedType.isReferenceType()) {
            return Lists.newArrayList();
        }
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();

        if (!referenceType.getTypeDeclaration().isPresent()) {
            return Lists.newArrayList();
        }
        ResolvedReferenceTypeDeclaration referenceTypeDeclaration = referenceType.getTypeDeclaration().get();

        if (referenceTypeDeclaration.getAllAncestors().stream()
                .anyMatch(ancestor -> ancestor.getId().equals("java.util.Collection"))
                && referenceType.getTypeParametersMap().size() == 1) {
            ResolvedType typeParameterOfCollection = referenceType.getTypeParametersMap().get(0).b;
            return this.analyzeEnumConstants(typeParameterOfCollection);
        }

        if (!referenceTypeDeclaration.isEnum()) {
            return Lists.newArrayList();
        }

        // 类加载枚举，用于调用getCode、getTitle方法
        Class<?> enumClass;
        try {
            enumClass = LoadClassUtils.loadClass(referenceTypeDeclaration.getQualifiedName(),
                    AstForestContext.get().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.warn("class '{}' not found", referenceTypeDeclaration.getQualifiedName(), e);
            return Lists.newArrayList();
        }

        List<AnalyzeEnumConstantsRetval> result = Lists.newArrayList();
        for (Object enumConstant : enumClass.getEnumConstants()) {
            AnalyzeEnumConstantsRetval aecRetval = analyzeEnumConstant(enumConstant);
            if (aecRetval != null) {
                result.add(aecRetval);
            }
        }
        return result;
    }

    protected AnalyzeEnumConstantsRetval analyzeEnumConstant(Object enumConstant) {
        if (MethodUtils.getAccessibleMethod(enumConstant.getClass(), config.getGetEnumCodeMethodName()) == null
                || MethodUtils.getAccessibleMethod(enumConstant.getClass(), config.getGetEnumTitleMethodName())
                == null) {
            log.info("EnumConstant '{}' has no method named '{}' nor {}", enumConstant,
                    config.getGetEnumCodeMethodName(), config.getGetEnumTitleMethodName());
            return null;
        }
        try {
            AnalyzeEnumConstantsRetval retval = new AnalyzeEnumConstantsRetval();
            retval.setCode(MethodUtils.invokeMethod(enumConstant, config.getGetEnumCodeMethodName()).toString());
            retval.setTitle(MethodUtils.invokeMethod(enumConstant, config.getGetEnumTitleMethodName()).toString());
            return retval;
        } catch (Exception e) {
            log.error("EnumConstant '{}' cannot invoke method", enumConstant, e);
            throw new Allison1875Exception(e);
        }
    }

}