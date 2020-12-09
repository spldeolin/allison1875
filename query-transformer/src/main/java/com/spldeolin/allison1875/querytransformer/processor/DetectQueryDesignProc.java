package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
public class DetectQueryDesignProc {

    public Collection<MethodCallExpr> process(AstForest astForest, String terminalMethodName) {
        Collection<MethodCallExpr> mces = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
                if (mce.getNameAsString().equals(terminalMethodName) && mce.getParentNode().isPresent()) {
                    mces.add(mce);
                }
            }
        }
        return mces;
    }

}