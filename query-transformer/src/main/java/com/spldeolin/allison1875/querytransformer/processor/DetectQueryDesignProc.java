package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
public class DetectQueryDesignProc {

    public List<MethodCallExpr> process(CompilationUnit cu) {
        List<MethodCallExpr> mces = Lists.newArrayList();
        for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
            if (StringUtils.equalsAny(mce.getNameAsString(), "many", "one", "over", "count") && mce.getParentNode()
                    .isPresent()) {
                mces.add(mce);
            }
        }
        return mces;
    }

    public MethodCallExpr processFirst(CompilationUnit cu) {
        return Iterables.getFirst(process(cu), null);
    }

}