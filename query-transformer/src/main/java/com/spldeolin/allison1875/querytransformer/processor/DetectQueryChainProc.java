package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
public class DetectQueryChainProc {

    @Inject
    private QueryTransformerConfig config;

    public List<MethodCallExpr> process(Node node) {
        List<MethodCallExpr> mces = Lists.newArrayList();
        for (MethodCallExpr mce : node.findAll(MethodCallExpr.class)) {
            if (StringUtils.equalsAny(mce.getNameAsString(), "many", "one", "over", "count") && mce.getParentNode()
                    .isPresent()) {
                if (this.finalNameExprRecursively(mce, config.getDesignPackage())) {
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
            if (scope.get().isFieldAccessExpr()) {
                Expression scopeOfFae = scope.get().asFieldAccessExpr().getScope();
                if (scopeOfFae.isMethodCallExpr()) {
                    return finalNameExprRecursively(scopeOfFae.asMethodCallExpr(), untilNameExprMatchedQualifier);
                }
            }
            if (scope.get().isNameExpr()) {
                NameExpr nameExpr = scope.get().asNameExpr();
                try {
                    String describe = nameExpr.calculateResolvedType().describe();
                    if (describe.startsWith(untilNameExprMatchedQualifier + ".")) {
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