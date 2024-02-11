package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Map;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultGenerationDto;
import com.spldeolin.allison1875.querytransformer.service.GenerateResultService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Slf4j
public class GenerateResultServiceImpl implements GenerateResultService {

    @Inject
    private QueryTransformerConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public ResultGenerationDto generate(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest) {
        boolean isAssigned = isAssigned(chainAnalysis);
        ResultGenerationDto result = new ResultGenerationDto();

        if (Lists.newArrayList(ChainMethodEnum.update, ChainMethodEnum.drop).contains(chainAnalysis.getChainMethod())) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.count) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (isAssigned) {
            if (Lists.newArrayList(ReturnClassifyEnum.many, ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)
                    .contains(chainAnalysis.getReturnClassify())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + designMeta.getEntityQualifier() + ">"));
                result.setElementTypeQualifier(designMeta.getEntityQualifier());
            } else {
                result.setResultType(StaticJavaParser.parseType(designMeta.getEntityQualifier()));
                result.setElementTypeQualifier(designMeta.getEntityQualifier());
            }
            return result;
        }

        Map<String, PropertyDto> properties = designMeta.getProperties();

        Set<PhraseDto> phrases = chainAnalysis.getQueryPhrases();
        log.info("queryPhrases.size()={}", chainAnalysis.getQueryPhrases().size());
        if (phrases.size() > 1) {
            // 指定了2个及以上属性，生成一个Javabean作为返回值类型
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(astForest);
            javabeanArg.setPackageName(config.getMapperRecordPackage());
            if (config.getEnableLotNoAnnounce()) {
                javabeanArg.setDescription(chainAnalysis.getLotNo());
            }
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Record");
            javabeanArg.setAuthorName(config.getAuthor());
            for (PhraseDto phrase : phrases) {
                String propertyName = phrase.getSubjectPropertyName();
                String varName = propertyName;
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(properties.get(propertyName).getDescription());
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            javabeanArg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration recordGeneration = javabeanGeneratorService.generate(javabeanArg);
            result.setFlush(recordGeneration.getFileFlush());
            result.setElementTypeQualifier(recordGeneration.getJavabeanQualifier());
            if (Lists.newArrayList(ReturnClassifyEnum.many, ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)
                    .contains(chainAnalysis.getReturnClassify())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + recordGeneration.getJavabeanQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(recordGeneration.getJavabeanQualifier()));
            }
            return result;

        } else if (phrases.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            String propertyName = Iterables.getOnlyElement(phrases).getSubjectPropertyName();
            JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
            result.setElementTypeQualifier(javaType.getQualifier());
            if (Lists.newArrayList(ReturnClassifyEnum.many, ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)
                    .contains(chainAnalysis.getReturnClassify())) {
                result.setResultType(StaticJavaParser.parseType("java.util.List<" + javaType.getQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(javaType.getQualifier()));
            }
            return result;

        } else {
            // 没有指定属性，使用Entity作为返回值类型
            result.setElementTypeQualifier(designMeta.getEntityQualifier());
            if (Lists.newArrayList(ReturnClassifyEnum.many, ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)
                    .contains(chainAnalysis.getReturnClassify())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + designMeta.getEntityQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(designMeta.getEntityQualifier()));
            }
            return result;
        }
    }

    private boolean isAssigned(ChainAnalysisDto chainAnalysis) {
        if (chainAnalysis.getChain().getParentNode().isPresent()) {
            return chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(parent -> parent instanceof VariableDeclarationExpr).isPresent();
        }
        return false;
    }

}