package com.spldeolin.allison1875.startransformer.processor;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainProcessor {

    @Inject
    private StarTransformerConfig config;

    @Inject
    private DetectStarChainProc detectStarChainProc;

    @Inject
    private AnalyzeChainProc analyzeChainProc;

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (CompilationUnit cu : astForest) {
            for (MethodCallExpr starChain : detectStarChainProc.process(cu)) {
                // analyze chain
                ChainAnalysisDto chainAnalysis;
                try {
                    chainAnalysis = analyzeChainProc.process(starChain, astForest);
                    log.info("chainAnalysis={}", chainAnalysis);
                } catch (Exception e) {
                    log.error("illegal chain: " + e.getMessage());
                    return;
                }

                // transform 'XxxWholeDto' Javabean
                JavabeanArg javabeanArg = new JavabeanArg();
                javabeanArg.setAstForest(astForest);
                javabeanArg.setPackageName(config.getWholeDtoPackge());
                javabeanArg.setDescription("");
                javabeanArg.setClassName(chainAnalysis.getWholeDtoName());
                FieldArg cftFieldArg = new FieldArg();
                cftFieldArg.setTypeQualifier(chainAnalysis.getCftEntityQualifier());
                cftFieldArg.setTypeName(chainAnalysis.getCftEntityName());
                cftFieldArg.setFieldName(this.entityNameToFieldName(chainAnalysis.getCftEntityName()));
                javabeanArg.getFieldArgs().add(cftFieldArg);
                for (PhraseDto phrase : chainAnalysis.getPhrases()) {
                    FieldArg dtFieldArg = new FieldArg();
                    dtFieldArg.setTypeQualifier(phrase.getDtEntityQualifier());
                    if (phrase.getIsOneToOne()) {
                        dtFieldArg.setTypeName(phrase.getDtEntityName());
                        dtFieldArg.setFieldName(this.entityNameToFieldName(phrase.getDtEntityName()));
                    } else {
                        dtFieldArg.setTypeName("List<" + phrase.getDtEntityName() + ">");
                        dtFieldArg.setFieldName(English.plural(this.entityNameToFieldName(phrase.getDtEntityName())));
                    }
                    javabeanArg.getFieldArgs().add(dtFieldArg);
                    if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(phrase.getMkeys())) {
                        for (String key : phrase.getKeys()) {
                            FieldArg keyFieldArg = new FieldArg();
                            keyFieldArg.setTypeName("Map<" + phrase.getEntityFieldTypesEachFieldName().get(key) + ","
                                    + phrase.getDtEntityName() + ">");
                            keyFieldArg.setFieldName(
                                    English.plural(this.entityNameToFieldName(phrase.getDtEntityName())) + "Each"
                                            + StringUtils.capitalize(key));
                            javabeanArg.getFieldArgs().add(keyFieldArg);
                        }
                        for (String mkey : phrase.getMkeys()) {
                            FieldArg mkeyFieldArg = new FieldArg();
                            mkeyFieldArg.setTypeName(
                                    "Multimap<" + phrase.getEntityFieldTypesEachFieldName().get(mkey) + ","
                                            + phrase.getDtEntityName() + ">");
                            mkeyFieldArg.setFieldName(
                                    English.plural(this.entityNameToFieldName(phrase.getDtEntityName())) + "Each"
                                            + StringUtils.capitalize(mkey));
                            javabeanArg.getFieldArgs().add(mkeyFieldArg);
                        }
                    }
                }
                CompilationUnit wholeDtoCu = JavabeanFactory.buildCu(javabeanArg);
                Saves.add(wholeDtoCu);

                // TODO transform Query Chain and replace Star Chain

                Saves.saveAll();
                detected++;
            }
        }
        if (detected == 0) {
            log.warn("no valid Chain transformed");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private String entityNameToFieldName(String entityName) {
        return MoreStringUtils.lowerFirstLetter(StringUtils.removeEnd(entityName, "Entity"));
    }

}