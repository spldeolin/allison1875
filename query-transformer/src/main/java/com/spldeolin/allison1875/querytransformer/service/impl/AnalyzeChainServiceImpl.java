package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.service.AnalyzeChainService;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class AnalyzeChainServiceImpl implements AnalyzeChainService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Override
    public ChainAnalysisDto analyze(MethodCallExpr chain, ClassOrInterfaceDeclaration design, DesignMeta designMeta)
            throws IllegalChainException {
        String chainCode = chain.toString();
        String betweenCode = chainCode.substring(chainCode.indexOf(".") + 1, chainCode.lastIndexOf("."));
        String designQualifier = design.getFullyQualifiedName().orElseThrow(() -> new QualifierAbsentException(design));

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

        String methodName = this.analyzeSpecifiedMethodName(chainMethod, chain, designMeta);

        ReturnClassifyEnum returnClassify;
        String keyPropertyName = null;
        if (chain.getNameAsString().equals("one")) {
            returnClassify = ReturnClassifyEnum.one;
        } else if (chain.getNameAsString().equals("many")) {
            if (CollectionUtils.isNotEmpty(chain.getArguments())) {
                returnClassify = ReturnClassifyEnum.many;
            } else if (chain.getArgument(0).asFieldAccessExpr().getScope().toString().equals("Each")) {
                returnClassify = ReturnClassifyEnum.each;
                keyPropertyName = chain.getArgument(0).asFieldAccessExpr().getNameAsString();
            } else if (chain.getArgument(0).asFieldAccessExpr().getScope().toString().equals("MultiEach")) {
                returnClassify = ReturnClassifyEnum.multiEach;
                keyPropertyName = chain.getArgument(0).asFieldAccessExpr().getNameAsString();
            } else {
                throw new IllegalChainException("many() argument is none of each nor multiEach");
            }
        } else if (chain.getNameAsString().equals("count")) {
            returnClassify = ReturnClassifyEnum.count;
        } else {
            returnClassify = null;
        }
        log.info("chainMethod={} returnClassify={}", chainMethod, returnClassify);

        Set<PhraseDto> queryPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> byPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> orderPhrases = Sets.newLinkedHashSet();
        Set<PhraseDto> updatePhrases = Sets.newLinkedHashSet();
        List<String> varNames = Lists.newArrayList();
        for (FieldAccessExpr fae : chain.findAll(FieldAccessExpr.class, TreeTraversal.POSTORDER)) {
            if (!designMeta.getProperties().containsKey(fae.getNameAsString())) {
                // 例如：XxxxDesign.query("xx").by().privilegeCode.in(Lists.newArrayList(OneTypeEnum.FIRST.getCode()))
                // .many();，其中的OneTypeEnum.FIRST应当被跳过
                continue;
            }
            String describe = fae.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".QueryChain")) {
                queryPhrases.add(new PhraseDto().setSubjectPropertyName(fae.getNameAsString()));
            }
            if (describe.startsWith(ByChainPredicate.class.getName()) && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                PredicateEnum predicate = PredicateEnum.of(parent.getNameAsString());
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setPredicate(predicate);
                if (predicate != PredicateEnum.IS_NULL && predicate != PredicateEnum.NOT_NULL) {
                    String varName = antiDuplicationService.getNewElementIfExist(fae.getNameAsString(), varNames);
                    phrase.setVarName(varName);
                }
                if (CollectionUtils.isNotEmpty(parent.getArguments())) {
                    phrase.setObjectExpr(parent.getArgument(0));
                }

                /*
                    将分析过的in()和nin()中的实际参数替换为null，
                    以确保后续的in()和nin()出现在scope的mce或fae进行calculateResolvedType时，不会因无法解析泛型而抛出异常
                 */
                if (Lists.newArrayList(predicate, PredicateEnum.IN, PredicateEnum.NOT_IN).contains(predicate)) {
                    parent.setArgument(0, new NullLiteralExpr());
                }

                byPhrases.add(phrase);
            }
            if (describe.startsWith(OrderChainPredicate.class.getName())) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                PredicateEnum predicate = PredicateEnum.of(parent.getNameAsString());
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(fae.getNameAsString());
                phrase.setPredicate(predicate);
                orderPhrases.add(phrase);
            }
        }
        List<String> queryPropertyNames = queryPhrases.stream().map(PhraseDto::getSubjectPropertyName)
                .collect(Collectors.toList());
        if (keyPropertyName != null && CollectionUtils.isNotEmpty(queryPropertyNames) && !queryPropertyNames.contains(
                keyPropertyName)) {
            log.warn("Each or MultiEach Key [{}] is not declared in Query Phrases [{}], auto add in", keyPropertyName,
                    queryPropertyNames);
            queryPhrases.add(new PhraseDto().setSubjectPropertyName(keyPropertyName));
        }
        for (MethodCallExpr mce : chain.findAll(MethodCallExpr.class, TreeTraversal.POSTORDER)) {
            String describe = mce.calculateResolvedType().describe();
            if (describe.startsWith(designQualifier + ".NextableUpdateChain")) {
                PhraseDto phrase = new PhraseDto();
                phrase.setSubjectPropertyName(mce.getNameAsString());
                String varName = antiDuplicationService.getNewElementIfExist(mce.getNameAsString(), varNames);
                phrase.setVarName(varName);
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
        result.setReturnClassify(returnClassify);
        result.setQueryPhrases(queryPhrases);
        result.setByPhrases(byPhrases);
        result.setOrderPhrases(orderPhrases);
        result.setUpdatePhrases(updatePhrases);
        result.setChain(chain);
        result.setIsByForced(chainCode.contains("." + TokenWordConstant.BY_FORCED_METHOD_NAME + "()"));
        String hash = StringUtils.upperCase(HashingUtils.hashString(result.toString()));
        result.setLotNo(String.format("QT%s-%s", Allison1875.SHORT_VERSION, hash));
        return result;
    }

    private String analyzeSpecifiedMethodName(ChainMethodEnum chainMethod, MethodCallExpr chain,
            DesignMeta designMeta) {
        MethodCallExpr queryMce = chain.findAll(MethodCallExpr.class,
                mce -> StringUtils.equalsAny(mce.getNameAsString(), "query", "update", "drop")).get(0);
        NodeList<Expression> arguments = queryMce.getArguments();
        if (CollectionUtils.isEmpty(arguments)) {
            String defaultMethodName = chainMethod.name() + StringUtils.removeEnd(designMeta.getEntityName(), "Entity");
            log.info("Method name not specified in Query Chain, hence default '{}' is used", defaultMethodName);
            return defaultMethodName;
        }
        String methodName = arguments.get(0).asStringLiteralExpr().getValue().trim();
        return methodName;
    }

}