package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParamGenerationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.service.GenerateParamService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Slf4j
public class GenerateParamServiceImpl implements GenerateParamService {

    @Inject
    private QueryTransformerConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public ParamGenerationDto generate(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest) {
        Map<String, PropertyDto> properties = designMeta.getProperties();

        List<Parameter> params = Lists.newArrayList();
        boolean isJavabean = false;
        FileFlush condFlush = null;

        Set<PhraseDto> phrases = Sets.newLinkedHashSet(chainAnalysis.getUpdatePhrases());
        phrases.addAll(chainAnalysis.getByPhrases());
        log.info("phrases.size()={}", phrases.size());
        if (phrases.stream().filter(p -> !Lists.newArrayList(PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL)
                .contains(p.getPredicate())).count() > 3) {
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(astForest);
            javabeanArg.setPackageName(config.getMapperConditionPackage());
            if (config.getEnableLotNoAnnounce()) {
                javabeanArg.setDescription(chainAnalysis.getLotNo());
            }
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Cond");
            javabeanArg.setAuthorName(config.getAuthor());
            for (PhraseDto phrase : phrases) {
                if (Lists.newArrayList(PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL).contains(phrase.getPredicate())) {
                    continue;
                }
                String propertyName = phrase.getSubjectPropertyName();
                String varName = phrase.getVarName();
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(properties.get(propertyName).getDescription());
                if (Lists.newArrayList(PredicateEnum.IN, PredicateEnum.NOT_IN).contains(phrase.getPredicate())) {
                    fieldArg.setTypeQualifier("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    fieldArg.setTypeQualifier(javaType.getQualifier());
                }
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            javabeanArg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration condGeneration = javabeanGeneratorService.generate(javabeanArg);
            condFlush = condGeneration.getFileFlush();
            Parameter param = new Parameter();
            param.setType(condGeneration.getJavabeanQualifier());
            param.setName(MoreStringUtils.lowerFirstLetter(condGeneration.getJavabeanName()));
            params.add(param);
            isJavabean = true;
        } else if (CollectionUtils.isNotEmpty(phrases)) {
            for (PhraseDto phrase : phrases) {
                if (Lists.newArrayList(PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL).contains(phrase.getPredicate())) {
                    continue;
                }
                String propertyName = phrase.getSubjectPropertyName();
                String varName = phrase.getVarName();
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                Parameter param = new Parameter();
                param.addAnnotation(StaticJavaParser.parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", varName)));

                if (Lists.newArrayList(PredicateEnum.IN, PredicateEnum.NOT_IN).contains(phrase.getPredicate())) {
                    param.setType("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    param.setType(javaType.getQualifier());
                }
                param.setName(varName);
                params.add(param);
            }
        } else {
            return new ParamGenerationDto();
        }

        ParamGenerationDto result = new ParamGenerationDto();
        result.getParameters().addAll(params);
        result.setIsCond(isJavabean);
        result.setCondFlush(condFlush);
        return result;
    }

}