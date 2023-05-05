package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.querytransformer.StarTransformerModule;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.support.StarSchema;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainProcessor {

    private static final AtomicInteger detected = new AtomicInteger(0);

    public static void main(String[] args) {
        Allison1875.allison1875(StarTransformer.class, new StarTransformerModule(null));
    }

    @Override
    public void process(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            tryDetectAndTransform(astForest, cu);
        }
        if (detected.get() == 0) {
            log.warn("no valid Chain detected");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private void tryDetectAndTransform(AstForest astForest, CompilationUnit cu) {
        // detect chain
        MethodCallExpr chain = detectStarChainProc(cu);
        if (chain == null) {
            return;
        }

//         mce.getScope().get().asMethodCallExpr().getScope().get().asMethodCallExpr().getScope().get()
//         .asMethodCallExpr().getScope().get().asMethodCallExpr().getScope().get().asMethodCallExpr().getScope().get
//         ().asMethodCallExpr().getScope().get().asMethodCallExpr().getScope().get().calculateResolvedType()


        this.tryDetectAndTransform(astForest, cu);
    }

    private void analyzeChainMce(MethodCallExpr mce, List<PhraseDto> phrases) {
        if (mce.getScope().isPresent()) {
            Expression scope = mce.getScope().get();
            if (scope.isMethodCallExpr()) {
                if (ChainMethodEnum.oo.toString().equals(mce.getNameAsString())) {
                    PhraseDto phrase = new PhraseDto();
                    phrase.setChainMethod(ChainMethodEnum.oo);
                    phrase.setDtEntityName(
                            mce.getArgument(0).asMethodReferenceExpr().getScope().asNameExpr().getNameAsString());
                    phrase.setDtDesignName(phrase.getDtEntityName().replace("Entity", "Design"));
                    phrase.setDtDesignQulifier(""); // TODO
                    phrase.setOmKeys(Lists.newArrayList());
                    phrases.add(phrase);
                } else if (ChainMethodEnum.om.toString().equals(mce.getNameAsString())) {

                } else if (ChainMethodEnum.key.toString().equals(mce.getNameAsString())) {

                } else if (ChainMethodEnum.cft.toString().equals(mce.getNameAsString())) {

                } else {
                    throw new RuntimeException("impossible unless bug.");
                }
                this.analyzeChainMce(scope.asMethodCallExpr(), phrases);
            }
            if (scope.isNameExpr()) {
                return;
            }
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