package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.base.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.generator.JavabeanGenerator;
import com.spldeolin.allison1875.base.generator.javabean.FieldArg;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.generator.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.base.util.EqualsUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.service.TransformParameterService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Log4j2
public class TransformParameterServiceImpl implements TransformParameterService {

    @Inject
    private QueryTransformerConfig config;

    @Nullable
    @Override
    public ParameterTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            AstForest astForest, List<FileFlush> flushes) {
        Map<String, PropertyDto> properties = designMeta.getProperties();

        List<String> imports = Lists.newArrayList();
        List<Parameter> params = Lists.newArrayList();
        boolean isJavabean = false;

        Set<PhraseDto> phrases = Sets.newLinkedHashSet(chainAnalysis.getUpdatePhrases());
        phrases.addAll(chainAnalysis.getByPhrases());
        log.info("phrases.size()={}", phrases.size());
        if (phrases.stream()
                .filter(p -> !EqualsUtils.equalsAny(p.getPredicate(), PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL))
                .count() > 3) {
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(astForest);
            javabeanArg.setPackageName(config.getMapperConditionPackage());
            javabeanArg.setDescription(chainAnalysis.getLotNo().asJavadocDescription());
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Cond");
            javabeanArg.setAuthorName(config.getAuthor());
            for (PhraseDto phrase : phrases) {
                if (EqualsUtils.equalsAny(phrase.getPredicate(), PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL)) {
                    continue;
                }
                String propertyName = phrase.getSubjectPropertyName();
                String varName = phrase.getVarName();
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setDescription(properties.get(propertyName).getDescription());
                if (EqualsUtils.equalsAny(phrase.getPredicate(), PredicateEnum.IN, PredicateEnum.NOT_IN)) {
                    fieldArg.setTypeName("Collection<" + javaType.getSimpleName() + ">");
                } else {
                    fieldArg.setTypeName(javaType.getSimpleName());
                }
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            javabeanArg.setEntityExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration javabeanGeneration = JavabeanGenerator.generate(javabeanArg);
            flushes.add(javabeanGeneration.getFileFlush());
            ClassOrInterfaceDeclaration cond = javabeanGeneration.getCoid();
            Parameter param = new Parameter();
            param.setType(cond.getNameAsString());
            param.setName(MoreStringUtils.lowerFirstLetter(cond.getNameAsString()));
            params.add(param);
            imports.add(cond.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            isJavabean = true;
        } else if (phrases.size() > 0) {
            for (PhraseDto phrase : phrases) {
                if (EqualsUtils.equalsAny(phrase.getPredicate(), PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL)) {
                    continue;
                }
                String propertyName = phrase.getSubjectPropertyName();
                String varName = phrase.getVarName();
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                Parameter param = new Parameter();
                param.addAnnotation(StaticJavaParser.parseAnnotation(String.format("@Param(\"%s\")", varName)));

                if (EqualsUtils.equalsAny(phrase.getPredicate(), PredicateEnum.IN, PredicateEnum.NOT_IN)) {
                    param.setType("Collection<" + javaType.getSimpleName() + ">");
                } else {
                    param.setType(javaType.getSimpleName());
                }
                param.setName(varName);
                imports.add(javaType.getQualifier());
                params.add(param);
            }
        } else {
            return null;
        }

        return new ParameterTransformationDto().setImports(imports).setParameters(params).setIsJavabean(isJavabean);
    }

}