package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.querytransformer.enums.OperatorEnum;
import com.spldeolin.allison1875.querytransformer.javabean.AnalyzeCriterionResultDto;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class AnalyzeCriterionProc {

    public AnalyzeCriterionResultDto process(MethodCallExpr mce, QueryMeta queryMeta) {
        List<MethodCallExpr> tokenMces = mce
                .findAll(MethodCallExpr.class, m -> m.getScope().filter(Expression::isFieldAccessExpr).isPresent());
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
            return new AnalyzeCriterionResultDto();
        }
        if (!Objects.equals(parts.pollFirst(), "over")) {
            log.warn("QueryDesign编写方式不正确");
            return new AnalyzeCriterionResultDto();
        }
        String queryMethodName = parts.pollLast();
        if (queryMethodName == null || !queryMethodName.startsWith("\"") || !queryMethodName.endsWith("\"")) {
            log.warn("QueryDesign的design方法必须使用String字面量作为实际参数");
            return new AnalyzeCriterionResultDto();
        }
        queryMethodName = queryMethodName.substring(1, queryMethodName.length() - 1);
        if (!Objects.equals(parts.pollLast(), "design")) {
            log.warn("QueryDesign编写方式不正确");
            return new AnalyzeCriterionResultDto();
        }

        Collection<CriterionDto> criterions = Lists.newArrayList();
        parts.descendingIterator().forEachRemaining(part -> {
            CriterionDto criterion;
            if (queryMeta.getPropertyNames().contains(part)) {
                criterion = new CriterionDto();
                criterions.add(criterion);
                criterion.setParameterName(part);
                criterion.setColumnName(StringUtils.lowerCamelToUnderscore(part));
                criterion.setDollarParameterName("#{" + part + "}");
            } else {
                criterion = Iterables.getLast(criterions);
                if (OperatorEnum.isValid(part)) {
                    criterion.setOperator(part);
                } else {
                    criterion.setArgumentExpr(part);
                }
            }
        });
        return new AnalyzeCriterionResultDto().setCriterions(criterions).setQueryMethodName(queryMethodName);
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

}