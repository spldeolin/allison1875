package com.spldeolin.allison1875.startransformer.processor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.support.StarSchema;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainProcessor {

    private static final AtomicInteger detected = new AtomicInteger(0);

    @Inject
    private StarTransformerConfig starTransformerConfig;

    @Override
    public void process(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            tryDetectAndTransform(cu);
        }
        if (detected.get() == 0) {
            log.warn("no valid Chain detected");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private void tryDetectAndTransform(CompilationUnit cu) {
        // detect chain
        MethodCallExpr chain = detectStarChainProc(cu);
        if (chain == null) {
            return;
        }

        // analyze chain
        ChainAnalysisDto chainAnalysis = analyzeChainMce(chain, Lists.newArrayList(), Lists.newArrayList(),
                Lists.newArrayList());
        log.info("chainAnalysis={}", chainAnalysis);

        // TODO transform Query Chain and replace Star Chain


        this.tryDetectAndTransform(cu);
    }

    private ChainAnalysisDto analyzeChainMce(MethodCallExpr mce, List<PhraseDto> phrases, List<String> keys,
            List<String> mkeys) {
        if (ChainMethodEnum.oo.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(true);
            phrase.setDtEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            phrase.setDtDesignName(phrase.getDtEntityName().replace("Entity", "Design"));
            phrase.setDtDesignQulifier(starTransformerConfig.getDesignPackage() + "." + phrase.getDtDesignName());
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            phrase.setFk(CodeGenerationUtils.getterToPropertyName(getterName));
            // One to One的维度表无法指定任何key，因为只有一条数据，没有意义
            phrase.setKeys(Lists.newArrayList());
            phrase.setMkeys(Lists.newArrayList());
            phrases.add(phrase);
        }
        if (ChainMethodEnum.om.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(false);
            phrase.setDtEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            phrase.setDtDesignName(phrase.getDtEntityName().replace("Entity", "Design"));
            phrase.setDtDesignQulifier(starTransformerConfig.getDesignPackage() + "." + phrase.getDtDesignName());
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            phrase.setFk(CodeGenerationUtils.getterToPropertyName(getterName));
            // 递归到此时，收集到的keys和mkeys均属于这个dt，组装完毕后需要清空并重新收集
            phrase.setKeys(Lists.newArrayList(keys));
            phrase.setMkeys(Lists.newArrayList(mkeys));
            phrases.add(phrase);
            keys.clear();
            mkeys.clear();
        }
        if (ChainMethodEnum.key.toString().equals(mce.getNameAsString())) {
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            keys.add(CodeGenerationUtils.getterToPropertyName(getterName));
        }
        if (ChainMethodEnum.mkey.toString().equals(mce.getNameAsString())) {
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            mkeys.add(CodeGenerationUtils.getterToPropertyName(getterName));
        }
        if (ChainMethodEnum.cft.toString().equals(mce.getNameAsString())) {
            ChainAnalysisDto chainAnalysis = new ChainAnalysisDto();
            chainAnalysis.setCftEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            chainAnalysis.setCftDesignName(chainAnalysis.getCftEntityName().replace("Entity", "Design"));
            chainAnalysis.setCftDesignQulifier(
                    starTransformerConfig.getDesignPackage() + "." + chainAnalysis.getCftDesignName());
            chainAnalysis.setCftSecondArgument(mce.getArgument(1));
            chainAnalysis.setPhrases(phrases);
            return chainAnalysis;
        }
        if (mce.getScope().filter(Expression::isMethodCallExpr).isPresent()) {
            return this.analyzeChainMce(mce.getScope().get().asMethodCallExpr(), phrases, keys, mkeys);
        }
        throw new RuntimeException("impossible unless bug.");
    }

    private MethodCallExpr detectStarChainProc(CompilationUnit cu) {
        for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
            if ("over".equals(mce.getNameAsString()) && mce.getParentNode().isPresent()) {
                if (this.finalNameExprRecursively(mce, StarSchema.class.getName())) {
                    return mce;
                }
            }
        }
        return null;
    }

    private boolean finalNameExprRecursively(MethodCallExpr mce, String untilNameExprMatchedQualifier) {
        Optional<Expression> scope = mce.getScope();
        if (scope.isPresent()) {
            if (scope.get().isMethodCallExpr()) {
                return finalNameExprRecursively(scope.get().asMethodCallExpr(), untilNameExprMatchedQualifier);
            }
            if (scope.get().isNameExpr()) {
                NameExpr nameExpr = scope.get().asNameExpr();
                try {
                    String describe = nameExpr.calculateResolvedType().describe();
                    if (untilNameExprMatchedQualifier.equals(describe)) {
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

}