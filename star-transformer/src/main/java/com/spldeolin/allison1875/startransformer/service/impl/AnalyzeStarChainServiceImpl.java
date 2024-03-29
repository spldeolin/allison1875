package com.spldeolin.allison1875.startransformer.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.common.util.NamingUtils;
import com.spldeolin.allison1875.startransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.AnalyzeStarChainService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-12
 */
@Singleton
@Log4j2
public class AnalyzeStarChainServiceImpl implements AnalyzeStarChainService {

    @Override
    public StarAnalysisDto analyze(MethodCallExpr starChain, AstForest astForest) throws IllegalChainException {
        return this.analyzeRecursively(starChain, astForest, Lists.newArrayList(), Lists.newArrayList(),
                Lists.newArrayList());
    }

    @Override
    public String buildWholeDtoNameFromEntityName(String entityName) {
        if (entityName.endsWith("Entity")) {
            return MoreStringUtils.replaceLast(entityName, "Entity", "WholeDto");
        } else {
            return entityName + "WholeDto";
        }
    }

    private StarAnalysisDto analyzeRecursively(MethodCallExpr mce, AstForest astForest, List<PhraseDto> phrases,
            List<String> keys, List<String> mkeys) throws IllegalChainException {
        if (ChainMethodEnum.oo.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(true);
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            phrase.setDtEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            phrase.setDtEntityName(NamingUtils.qualifierToTypeName(phrase.getDtEntityQualifier()));
            phrase.setDtDesignName(fae.getScope().toString());
            phrase.setFk(fae.getNameAsString());
            phrase.setFkTypeQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("K").get().describe());
            // One to One的维度表无法指定任何key，因为只有一条数据，没有意义
            phrase.setKeys(Lists.newArrayList());
            phrase.setMkeys(Lists.newArrayList());
            phrases.add(phrase);
        }
        if (ChainMethodEnum.om.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(false);
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            phrase.setDtEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            phrase.setDtEntityName(NamingUtils.qualifierToTypeName(phrase.getDtEntityQualifier()));
            phrase.setDtDesignName(fae.getScope().toString());
            phrase.setFk(fae.getNameAsString());
            phrase.setFkTypeQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("K").get().describe());
            // 递归到此时，收集到的keys和mkeys均属于这个dt，组装完毕后需要清空并重新收集
            phrase.setKeys(Lists.newArrayList(keys));
            phrase.setMkeys(Lists.newArrayList(mkeys));
            if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(phrase.getMkeys())) {
                for (VariableDeclarator vd : astForest.findCu(phrase.getDtEntityQualifier())
                        .findAll(VariableDeclarator.class)) {
                    phrase.getEntityFieldTypesEachFieldName().put(vd.getNameAsString(), vd.getTypeAsString());
                }
            }
            phrases.add(phrase);
            keys.clear();
            mkeys.clear();
        }
        if (ChainMethodEnum.key.toString().equals(mce.getNameAsString())) {
            keys.add(mce.getArgument(0).asFieldAccessExpr().getNameAsString());
        }
        if (ChainMethodEnum.mkey.toString().equals(mce.getNameAsString())) {
            mkeys.add(mce.getArgument(0).asFieldAccessExpr().getNameAsString());
        }
        if (ChainMethodEnum.cft.toString().equals(mce.getNameAsString())) {
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            StarAnalysisDto analysis = new StarAnalysisDto();
            analysis.setCftEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            analysis.setCftEntityName(NamingUtils.qualifierToTypeName(analysis.getCftEntityQualifier()));
            analysis.setCftDesignName(fae.getScope().toString());
            analysis.setCftDesignQualifier(fae.getScope().calculateResolvedType().describe());
            analysis.setCftSecondArgument(mce.getArgument(1));
            analysis.setPhrases(phrases);
            String wholeDtoName = this.buildWholeDtoNameFromEntityName(analysis.getCftEntityName());
            analysis.setWholeDtoName(wholeDtoName);
            String hash = StringUtils.upperCase(HashingUtils.hashString(JsonUtils.toJson(analysis)));
            analysis.setLotNo(String.format("ST%s-%s", Allison1875.SHORT_VERSION, hash));
            return analysis;
        }
        if (mce.getScope().filter(Expression::isMethodCallExpr).isPresent()) {
            return this.analyzeRecursively(mce.getScope().get().asMethodCallExpr(), astForest, phrases, keys, mkeys);
        }
        throw new IllegalChainException("impossible unless bug.");
    }

}