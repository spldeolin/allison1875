package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * @author Deolin 2020-10-10
 */
public class DetectQueryDesignProc {

    private final AstForest astForest;

    private final String terminalMethodName;

    private Collection<MethodCallExpr> mces;

    DetectQueryDesignProc(AstForest astForest, String terminalMethodName) {
        this.astForest = astForest;
        this.terminalMethodName = terminalMethodName;
    }

    public DetectQueryDesignProc process() {
        mces = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
                if (mce.getNameAsString().equals(terminalMethodName) && mce.getParentNode().isPresent()) {
                    mces.add(mce);
                }
            }
        }
        return this;
    }

    public Collection<MethodCallExpr> getMces() {
        return mces;
    }

}