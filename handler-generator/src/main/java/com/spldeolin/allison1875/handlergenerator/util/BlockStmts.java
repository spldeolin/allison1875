package com.spldeolin.allison1875.handlergenerator.util;

import java.util.Collection;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2020-06-26
 */
public class BlockStmts {

    public static Collection<Expression> listExpressions(NodeWithStatements<?> nodeWithStatements) {
        Collection<Expression> result = Lists.newArrayList();
        for (Statement statement : nodeWithStatements.getStatements()) {
            statement.ifExpressionStmt(exprStmt -> result.add(exprStmt.getExpression()));
        }
        return result;
    }

    public static <E extends Expression> Collection<E> listExpressions(NodeWithStatements<?> nodeWithStatements,
            Class<E> clazz) {
        Collection<E> result = Lists.newArrayList();
        for (Statement statement : nodeWithStatements.getStatements()) {
            statement.ifExpressionStmt(exprStmt -> {
                Expression expr = exprStmt.getExpression();
                if (expr.getClass() == clazz) {
                    result.add((E) expr);
                }
            });
        }
        return result;
    }

}
