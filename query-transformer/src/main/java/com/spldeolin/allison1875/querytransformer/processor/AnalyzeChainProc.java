package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.google.mu.util.Substring;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class AnalyzeChainProc {

    public ChainAnalysisDto process(MethodCallExpr chain, ClassOrInterfaceDeclaration design) {
        String chainCode = chain.toString();
        String betweenCode = Substring.between(Substring.first('.'), Substring.last(".")).from(chainCode)
                .orElseThrow(IllegalChainException::new);
        String designQualifier = design.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);

        MethodCallExpr queryMce = chain
                .findAll(MethodCallExpr.class, mce -> StringUtils.equalsAny(mce.getNameAsString(), "query", "update"))
                .get(0);
        String methodName = queryMce.getArguments().get(0).asStringLiteralExpr().getValue();
        log.info("methodName={}", methodName);

        boolean queryOrUpdate = betweenCode.startsWith("query(");
        boolean returnManyOrOne = chainCode.endsWith("many()");
        log.info("queryOrUpdate={} returnManyOrOne={}", queryOrUpdate, returnManyOrOne);

        Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();
        for (FieldAccessExpr fae : chain.findAll(FieldAccessExpr.class, TreeTraversal.POSTORDER)) {
            String describe = fae.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".QueryChain")) {
                queryPhrases.add(new PhraseDto().setSubjectPropertyName(fae.getNameAsString()));
            }
            if (describe.startsWith(ByChainPredicate.class.getName()) && fae.getParentNode().isPresent()) {
                Node parent = fae.getParentNode().get();
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setVerb(VerbEnum.of(((MethodCallExpr) parent).getNameAsString()));
                phrase.setObjectExpr(((MethodCallExpr) parent).getArgument(0));
                byPhrases.add(phrase);
            }
            if (describe.startsWith(OrderChainPredicate.class.getName())) {
                Node parent = fae.getParentNode().get();
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setVerb(VerbEnum.of(((MethodCallExpr) parent).getNameAsString()));
                orderPhrases.add(phrase);
            }
        }
        for (MethodCallExpr mce : chain.findAll(MethodCallExpr.class, TreeTraversal.POSTORDER)) {
            String describe = mce.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".NextableUpdateChain")) {
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(mce.getNameAsString());
                phrase.setObjectExpr(mce.getArgument(0));
                updatePhrases.add(phrase);
            }
        }
        log.info("queryPhrases={}", queryPhrases);
        log.info("byPhrases={}", byPhrases);
        log.info("orderPhrases={}", orderPhrases);
        log.info("updatePhrases={}", updatePhrases);

        ChainAnalysisDto result = new ChainAnalysisDto();
        result.setMethodName(methodName);
        result.setQueryOrUpdate(queryOrUpdate);
        result.setReturnManyOrOne(returnManyOrOne);
        result.setQueryPhrases(queryPhrases);
        result.setByPhrases(byPhrases);
        result.setOrderPhrases(orderPhrases);
        result.setUpdatePhrases(updatePhrases);

        List<MethodCallExpr> tokenMces = chain
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
        collectCondition(parts, chain);
        if (parts.size() < 3) {
            log.warn("QueryDesign编写方式不正确");
            return new ChainAnalysisDto();
        }
        if (!Objects.equals(parts.pollFirst(), "over")) {
            log.warn("QueryDesign编写方式不正确");
            return new ChainAnalysisDto();
        }
        String queryMethodName = parts.pollLast();
        if (queryMethodName == null || !queryMethodName.startsWith("\"") || !queryMethodName.endsWith("\"")) {
            log.warn("QueryDesign的design方法必须使用String字面量作为实际参数");
            return new ChainAnalysisDto();
        }
        queryMethodName = queryMethodName.substring(1, queryMethodName.length() - 1);
        if (!Objects.equals(parts.pollLast(), "design")) {
            log.warn("QueryDesign编写方式不正确");
            return new ChainAnalysisDto();
        }

        return new ChainAnalysisDto().setMethodName(queryMethodName);
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