package com.spldeolin.allison1875.startransformer;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.service.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.AnalyzeStarChainService;
import com.spldeolin.allison1875.startransformer.service.DetectStarChainService;
import com.spldeolin.allison1875.startransformer.service.GenerateWholeDtoService;
import com.spldeolin.allison1875.startransformer.service.TransformStarChainService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainService {

    @Inject
    private DetectStarChainService detectStarChainService;

    @Inject
    private AnalyzeStarChainService analyzeStarChainService;

    @Inject
    private GenerateWholeDtoService generateWholeDtoService;

    @Inject
    private TransformStarChainService transformStarChainService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);

            for (BlockStmt block : cu.findAll(BlockStmt.class)) {
                for (MethodCallExpr starChain : detectStarChainService.detect(block)) {

                    // analyze chain
                    StarAnalysisDto analysis;
                    try {
                        analysis = analyzeStarChainService.analyze(starChain, astForest);
                        log.info("Star Chain analyzed, analysis={}", analysis);
                    } catch (IllegalChainException e) {
                        log.error("fail to analyze Star Chain, starChain={}", starChain, e);
                        continue;
                    }

                    // generate 'XxxWholeDto' Javabean
                    JavabeanGeneration wholeDtoGeneration;
                    try {
                        wholeDtoGeneration = generateWholeDtoService.generate(astForest, analysis);
                        log.info("Whole DTO generated, qualifier={} path={}", wholeDtoGeneration.getJavabeanQualifier(),
                                wholeDtoGeneration.getPath());
                    } catch (Exception e) {
                        log.error("fail to generate Whole DTO, analysis={}", analysis, e);
                        continue;
                    }
                    flushes.add(wholeDtoGeneration.getFileFlush());

                    // transform Query Chain and replace Star Chain
                    try {
                        transformStarChainService.transformStarChain(block, analysis, starChain, wholeDtoGeneration);
                        log.info("Star Chain transformed");
                    } catch (Exception e) {
                        log.error("fail to transformStarChain Star Chain, starAnalysis={}", analysis, e);
                    }

                    // add import
                    cu.addImport(ImportConstant.GOOGLE_COMMON_COLLECTION);
                    cu.addImport(ImportConstant.JAVA_UTIL);
                    cu.addImport(wholeDtoGeneration.getJavabeanQualifier());
                    cu.addImport(analysis.getCftEntityQualifier());
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        cu.addImport(phrase.getDtEntityQualifier());
                    }
                    anyTransformed = true;
                }
            }
            if (anyTransformed) {
                flushes.add(FileFlush.buildLexicalPreserving(cu));
            }
        }

        // flush
        if (flushes.size() > 0) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}