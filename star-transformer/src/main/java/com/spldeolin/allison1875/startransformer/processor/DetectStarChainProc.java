package com.spldeolin.allison1875.startransformer.processor;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.support.StarSchema;

/**
 * @author Deolin 2023-05-12
 */
@Singleton
public class DetectStarChainProc {

    public List<MethodCallExpr> process(CompilationUnit cu) {
        List<MethodCallExpr> mces = Lists.newArrayList();
        for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
            if ("over".equals(mce.getNameAsString()) && mce.getParentNode().isPresent()) {
                if (this.finalNameExprRecursively(mce, StarSchema.class.getName())) {
                    mces.add(mce);
                }
            }
        }
        return mces;
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