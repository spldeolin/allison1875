package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.querytransformer.enums.OperatorEnum;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;

/**
 * @author Deolin 2020-10-10
 */
class AnalyzeCriterionProc {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(AnalyzeCriterionProc.class);

    private final MethodCallExpr mce;

    private final QueryMeta queryMeta;

    private Collection<CriterionDto> criterions;

    private String queryMethodName;

    AnalyzeCriterionProc(MethodCallExpr mce, QueryMeta queryMeta) {
        this.mce = mce;
        this.queryMeta = queryMeta;
    }

    AnalyzeCriterionProc process() {
        List<MethodCallExpr> tokenMces = mce
                .findAll(MethodCallExpr.class, mce -> mce.getScope().filter(Expression::isFieldAccessExpr).isPresent());
        Collections.reverse(tokenMces);

        for (MethodCallExpr tokenMce : tokenMces) {
            FieldAccessExpr fae = tokenMce.getScope().get().asFieldAccessExpr();
            log.info("criteria.parameterName={}", fae.getNameAsString());
            log.info("criteria.dollarParameterName={}", "${" + fae.getNameAsString() + "}");
            log.info("criteria.arguments={}", tokenMce.getArguments());
            log.info("criteria.operator={}", tokenMce.getNameAsString());
        }

        Deque<String> parts = Queues.newArrayDeque();
        collectCondition(parts, mce);
        if (parts.size() < 3) {
            log.warn("QueryDesign编写方式不正确");
            return this;
        }
        if (!Objects.equals(parts.pollFirst(), "over")) {
            log.warn("QueryDesign编写方式不正确");
            return this;
        }
        queryMethodName = parts.pollLast();
        if (queryMethodName == null || !queryMethodName.startsWith("\"") || !queryMethodName.endsWith("\"")) {
            log.warn("QueryDesign的design方法必须使用String字面量作为实际参数");
            return this;
        }
        queryMethodName = queryMethodName.substring(1, queryMethodName.length() - 1);
        if (!Objects.equals(parts.pollLast(), "design")) {
            log.warn("QueryDesign编写方式不正确");
            return this;
        }

        criterions = Lists.newArrayList();
        parts.descendingIterator().forEachRemaining(part -> {
            CriterionDto criterion;
            if (queryMeta.getPropertyNames().contains(part)) {
                criterion = new CriterionDto();
                criterions.add(criterion);
                criterion.propertyName(part);
                criterion.columnName(StringUtils.lowerCamelToUnderscore(part));
                criterion.dollarVar("#{" + part + "}");
            } else {
                criterion = Iterables.getLast(criterions);
                if (OperatorEnum.isValid(part)) {
                    criterion.operator(part);
                } else {
                    criterion.varName(part);
                }
            }
        });
        return this;
    }

    private void collectCondition(Deque<String> parts, Expression scope) {
        scope.ifMethodCallExpr(mce -> {
            String operator = mce.getNameAsString();
            parts.add(operator);
            NodeList<Expression> arguments = scope.asMethodCallExpr().getArguments();
            if (arguments.size() > 0) {
                parts.add(arguments.get(0).toString());
            }
            mce.getScope().ifPresent(scopeEx -> this.collectCondition(parts, scopeEx));
        });

        scope.ifFieldAccessExpr(fae -> {
            String propertyName = scope.asFieldAccessExpr().getNameAsString();
            parts.add(propertyName);
            this.collectCondition(parts, fae.getScope());
        });
    }

    public Collection<CriterionDto> getCriterions() {
        return criterions;
    }

    public String getQueryMethodName() {
        return queryMethodName;
    }

}