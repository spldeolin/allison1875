package com.spldeolin.allison1875.da.strategy;

import java.util.Optional;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-06-02
 */
public class ConcernedResponseBodyTypeStrategy {

    public ResolvedType findConcernedResponseBodyType(MethodDeclaration handler) {
        for (MethodCallExpr mce : handler.findAll(MethodCallExpr.class)) {
            Optional<Expression> scope = mce.getScope();
            if (scope.isPresent() && "setData".equals(mce.getNameAsString())) {
                if (scope.get().calculateResolvedType().describe().startsWith("")) {
                    if (!"null".equals(mce.getArgument(0).toString())) {
                        return mce.getArgument(0).calculateResolvedType();
                    }
                }
            }
        }
        return null;
    }

}
