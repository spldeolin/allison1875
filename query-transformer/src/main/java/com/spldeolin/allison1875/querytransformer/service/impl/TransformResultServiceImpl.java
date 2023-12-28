package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.EqualsUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.TransformResultService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Log4j2
public class TransformResultServiceImpl implements TransformResultService {

    @Inject
    private QueryTransformerConfig config;

    @Override
    public ResultTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest,
            List<FileFlush> flushes) {
        boolean isAssigned = isAssigned(chainAnalysis);
        ResultTransformationDto result = new ResultTransformationDto();

        if (EqualsUtils.equalsAny(chainAnalysis.getChainMethod(), ChainMethodEnum.update, ChainMethodEnum.drop)) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.count) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (isAssigned) {
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.many,
                    ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)) {
                result.setResultType(StaticJavaParser.parseType("List<" + designMeta.getEntityName() + ">"));
                result.setElementTypeQualifier(designMeta.getEntityQualifier());
            } else {
                result.setResultType(StaticJavaParser.parseType(designMeta.getEntityName()));
                result.setElementTypeQualifier(designMeta.getEntityQualifier());
            }
            result.getImports().add(designMeta.getEntityQualifier());
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
            javabeanArg.setDescription(chainAnalysis.getLotNo().asJavadocDescription());
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Record");
            javabeanArg.setAuthorName(config.getAuthor());
            for (PhraseDto phrase : phrases) {
                String propertyName = phrase.getSubjectPropertyName();
                String varName = propertyName;
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setDescription(properties.get(propertyName).getDescription() + chainAnalysis.getLotNo()
                        .asJavadocDescription());
                fieldArg.setTypeName(javaType.getSimpleName());
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            CompilationUnit cu = JavabeanFactory.buildCu(javabeanArg);
            flushes.add(FileFlush.build(cu));
            TypeDeclaration<?> resultType = cu.getPrimaryType().orElseThrow(RuntimeException::new);
            String javabeanQualifier = resultType.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
            result.setElementTypeQualifier(javabeanQualifier);
            result.getImports().add(javabeanQualifier);
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.many,
                    ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)) {
                result.setResultType(StaticJavaParser.parseType("List<" + resultType.getNameAsString() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(resultType.getNameAsString()));
            }
            return result;

        } else if (phrases.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            String propertyName = Iterables.getOnlyElement(phrases).getSubjectPropertyName();
            JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
            result.setElementTypeQualifier(javaType.getQualifier());
            result.setImports(Lists.newArrayList(javaType.getQualifier()));
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.many,
                    ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)) {
                result.setResultType(StaticJavaParser.parseType("List<" + javaType.getSimpleName() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(javaType.getSimpleName()));
            }
            return result;

        } else {
            // 没有指定属性，使用Entity作为返回值类型
            result.setElementTypeQualifier(designMeta.getEntityQualifier());
            result.setImports(Lists.newArrayList(designMeta.getEntityQualifier()));
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.many,
                    ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)) {
                result.setResultType(StaticJavaParser.parseType("List<" + designMeta.getEntityName() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(designMeta.getEntityName()));
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