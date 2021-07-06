package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import jodd.util.StringUtil;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class AnalyzeChainProc {

    public ChainAnalysisDto process(MethodCallExpr chain, ClassOrInterfaceDeclaration design) {
        String chainCode = chain.toString();
        String betweenCode = StringUtil.substring(chainCode, chainCode.indexOf(".") + 1, chainCode.lastIndexOf("."));
        String designQualifier = design.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);

        MethodCallExpr queryMce = chain
                .findAll(MethodCallExpr.class, mce -> StringUtils.equalsAny(mce.getNameAsString(), "query", "update"))
                .get(0);
        String methodName = queryMce.getArguments().get(0).asStringLiteralExpr().getValue();
        log.info("methodName={}", methodName);

        ChainMethodEnum chainMethod;
        if (betweenCode.startsWith("query(")) {
            chainMethod = ChainMethodEnum.query;
        } else if (betweenCode.startsWith("update(")) {
            chainMethod = ChainMethodEnum.update;
        } else if (betweenCode.startsWith("drop(")) {
            chainMethod = ChainMethodEnum.drop;
        } else {
            throw new IllegalChainException("chainMethod is none of query, update or drop");
        }
        boolean returnManyOrOne = chainCode.endsWith("many()");
        log.info("chainMethod={} returnManyOrOne={}", chainMethod, returnManyOrOne);

        Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();
        List<String> varNames = Lists.newArrayList();
        for (FieldAccessExpr fae : chain.findAll(FieldAccessExpr.class, TreeTraversal.POSTORDER)) {
            String describe = fae.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".QueryChain")) {
                queryPhrases.add(new PhraseDto().setSubjectPropertyName(fae.getNameAsString()));
            }
            if (describe.startsWith(ByChainPredicate.class.getName()) && fae.getParentNode().isPresent()) {
                Node parent = fae.getParentNode().get();
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setPredicate(PredicateEnum.of(((MethodCallExpr) parent).getNameAsString()));
                if (phrase.getPredicate() != PredicateEnum.IS_NULL && phrase.getPredicate() != PredicateEnum.NOT_NULL) {
                    phrase.setVarName(ensureNoRepeation(fae.getNameAsString(), varNames));
                }
                if (((MethodCallExpr) parent).getArguments().size() > 0) {
                    phrase.setObjectExpr(((MethodCallExpr) parent).getArgument(0));
                }
                byPhrases.add(phrase);
            }
            if (describe.startsWith(OrderChainPredicate.class.getName())) {
                Node parent = fae.getParentNode().get();
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setPredicate(PredicateEnum.of(((MethodCallExpr) parent).getNameAsString()));
                orderPhrases.add(phrase);
            }
        }
        for (MethodCallExpr mce : chain.findAll(MethodCallExpr.class, TreeTraversal.POSTORDER)) {
            String describe = mce.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".NextableUpdateChain")) {
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(mce.getNameAsString());
                phrase.setVarName(ensureNoRepeation(mce.getNameAsString(), varNames));
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
        result.setChainMethod(chainMethod);
        result.setReturnManyOrOne(returnManyOrOne);
        result.setQueryPhrases(queryPhrases);
        result.setByPhrases(byPhrases);
        result.setOrderPhrases(orderPhrases);
        result.setUpdatePhrases(updatePhrases);
        result.setChain(chain);
        result.setIndent(TokenRanges
                .getStartIndent(chain.findAncestor(ExpressionStmt.class).orElseThrow(IllegalChainException::new)));
        return result;
    }

    private String ensureNoRepeation(String name, List<String> names) {
        if (!names.contains(name)) {
            names.add(name);
            return name;
        }
        return ensureNoRepeation(name, names, 2);
    }

    private String ensureNoRepeation(String name, List<String> names, int index) {
        if (!names.contains(name + index)) {
            names.add(name + index);
            return name + index;
        }
        return this.ensureNoRepeation(name, names, index + 1);
    }

}