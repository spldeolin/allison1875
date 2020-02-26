package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.IfStmt;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-25
 */
public class LimitConditionLogicalOperatorStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> {
            cu.findAll(ConditionalExpr.class).forEach(ce -> this.check(result, ce));
            cu.findAll(IfStmt.class).forEach(is -> this.check(result, is));
        });
        return result;
    }

    private <T extends Node & NodeWithCondition<?>> void check(Collection<LawlessDto> result, T node) {
        int operatorCount = node.getCondition().findAll(BinaryExpr.class, be -> {
            Operator operator = be.getOperator();
            return operator == Operator.OR || operator == Operator.AND || operator == Operator.BINARY_OR
                    || operator == Operator.BINARY_AND;
        }).size();
        if (operatorCount >= 3) {
            result.add(new LawlessDto(node)
                    .setMessage(operatorCount + "个逻辑运算符。 if (" + node.getCondition().toString() + ")"));
        }
    }

}
