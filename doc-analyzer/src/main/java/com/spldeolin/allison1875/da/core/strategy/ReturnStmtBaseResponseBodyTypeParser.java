package com.spldeolin.allison1875.da.core.strategy;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-01-02
 */
public class ReturnStmtBaseResponseBodyTypeParser implements ResponseBodyTypeParser {

    @Override
    public ResolvedType parse(MethodDeclaration handler) {
        List<ReturnStmt> returnStmts = handler.findAll(ReturnStmt.class);
        for (ReturnStmt returnStmt : returnStmts) {
            Optional<Expression> expressionOpt = returnStmt.getExpression();
            if (expressionOpt.isPresent()) {
                Expression expression = expressionOpt.get();
                if (expression.isObjectCreationExpr()) {
                    ObjectCreationExpr objectCreationExpr = expression.asObjectCreationExpr();
                    if (objectCreationExpr.getArguments().size() == 1) {
                        Expression argument = objectCreationExpr.getArgument(0);
                        ResolvedType resolvedType = argument.calculateResolvedType();
                        return resolvedType;
                    }
                }
            }
        }
        return null;
    }

}
