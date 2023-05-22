package com.spldeolin.allison1875.startransformer.processor;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import jodd.io.FileNameUtil;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-12
 */
@Singleton
@Log4j2
public class AnalyzeChainProc {

    @Inject
    private StarTransformerConfig starTransformerConfig;

    public ChainAnalysisDto process(MethodCallExpr starChain, AstForest astForest, Set<String> wholeDtoNames)
            throws IllegalChainException {
        return process(starChain, astForest, wholeDtoNames, Lists.newArrayList(), Lists.newArrayList(),
                Lists.newArrayList());
    }

    private ChainAnalysisDto process(MethodCallExpr mce, AstForest astForest, Set<String> wholeDtoNames,
            List<PhraseDto> phrases, List<String> keys, List<String> mkeys) throws IllegalChainException {
        if (ChainMethodEnum.oo.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(true);
            phrase.setDtEntityQualifier(
                    mce.getArgument(0).asMethodReferenceExpr().getScope().calculateResolvedType().describe());
            phrase.setDtEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            phrase.setDtDesignName(phrase.getDtEntityName().replace("Entity", "Design"));
            phrase.setDtDesignQulifier(starTransformerConfig.getDesignPackage() + "." + phrase.getDtDesignName());
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            phrase.setFk(CodeGenerationUtils.getterToPropertyName(getterName));
            // One to One的维度表无法指定任何key，因为只有一条数据，没有意义
            phrase.setKeys(Lists.newArrayList());
            phrase.setMkeys(Lists.newArrayList());
            phrases.add(phrase);
        }
        if (ChainMethodEnum.om.toString().equals(mce.getNameAsString())) {
            PhraseDto phrase = new PhraseDto();
            phrase.setIsOneToOne(false);
            phrase.setDtEntityQualifier(
                    mce.getArgument(0).asMethodReferenceExpr().getScope().calculateResolvedType().describe());
            phrase.setDtEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            phrase.setDtDesignName(phrase.getDtEntityName().replace("Entity", "Design"));
            phrase.setDtDesignQulifier(starTransformerConfig.getDesignPackage() + "." + phrase.getDtDesignName());
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            phrase.setFk(CodeGenerationUtils.getterToPropertyName(getterName));
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
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            keys.add(CodeGenerationUtils.getterToPropertyName(getterName));
        }
        if (ChainMethodEnum.mkey.toString().equals(mce.getNameAsString())) {
            String getterName = mce.getArgument(0).asMethodReferenceExpr().getIdentifier();
            mkeys.add(CodeGenerationUtils.getterToPropertyName(getterName));
        }
        if (ChainMethodEnum.cft.toString().equals(mce.getNameAsString())) {
            ChainAnalysisDto analysis = new ChainAnalysisDto();
            analysis.setCftEntityName(mce.getArgument(0).asMethodReferenceExpr().getScope().toString());
            analysis.setCftEntityQualifier(
                    mce.getArgument(0).asMethodReferenceExpr().getScope().calculateResolvedType().describe());
            analysis.setCftDesignName(analysis.getCftEntityName().replace("Entity", "Design"));
            analysis.setCftDesignQualifier(
                    starTransformerConfig.getDesignPackage() + "." + analysis.getCftDesignName());
            analysis.setCftSecondArgument(mce.getArgument(1));
            analysis.setPhrases(phrases);
            String wholeDtoName = this.ensureNoRepeatInAstForest(astForest, wholeDtoNames,
                    analysis.getCftEntityName().replace("Entity", "WholeDto"));
            analysis.setWholeDtoName(wholeDtoName);
            wholeDtoNames.add(wholeDtoName);
            return analysis;
        }
        if (mce.getScope().filter(Expression::isMethodCallExpr).isPresent()) {
            return this.process(mce.getScope().get().asMethodCallExpr(), astForest, wholeDtoNames, phrases, keys,
                    mkeys);
        }
        throw new RuntimeException("impossible unless bug.");
    }

    /**
     * 确保参数coidName与在AstForest中所有的java文件名均不重名
     */
    private String ensureNoRepeatInAstForest(AstForest astForest, Set<String> wholeDtoNames, String coidName) {
        boolean conflicting = astForest.getJavasInForest().stream()
                .anyMatch(java -> FileNameUtil.getBaseName(java.toFile().getName()).equals(coidName));
        conflicting |= wholeDtoNames.contains(coidName);
        if (conflicting) {
            String rename = concatEx(coidName);
            log.info("File [{}.java] exist in AstForest, rename to [{}.java]", coidName, rename);
            return ensureNoRepeatInAstForest(astForest, wholeDtoNames, rename);
        } else {
            return coidName;
        }
    }

    private String concatEx(String coidName) {
        return coidName.replace("Dto", "ExDto");
    }

}