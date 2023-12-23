package com.spldeolin.allison1875.startransformer.processor;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainService {

    @Inject
    private DetectStarChainProc detectStarChainProc;

    @Inject
    private AnalyzeChainProc analyzeChainProc;

    @Inject
    private TransformWholeDtoProc transformWholeDtoProc;

    @Inject
    private TransformChainProc transformChainProc;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();
        Set<String> wholeDtoNames = Sets.newHashSet();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);
            for (BlockStmt block : cu.findAll(BlockStmt.class)) {
                for (MethodCallExpr starChain : detectStarChainProc.process(block)) {
                    // analyze chain
                    ChainAnalysisDto analysis;
                    try {
                        analysis = analyzeChainProc.process(starChain, astForest, wholeDtoNames);
                        log.info("chainAnalysis={}", analysis);
                    } catch (IllegalChainException e) {
                        log.error("illegal chain: " + e.getMessage());
                        continue;
                    }

                    // transform 'XxxWholeDto' Javabean
                    JavabeanArg javabeanArg = new JavabeanArg();
                    CompilationUnit wholeDtoCu = transformWholeDtoProc.transformWholeDto(javabeanArg, astForest,
                            analysis);
                    flushes.add(FileFlush.build(wholeDtoCu));

                    // transform Query Chain and replace Star Chain
                    transformChainProc.transformAndReplaceStar(block, analysis, starChain);

                    // add import
                    cu.addImport(javabeanArg.getPackageName() + "." + javabeanArg.getClassName());
                    cu.addImport(analysis.getCftEntityQualifier());
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        cu.addImport(phrase.getDtEntityQualifier());
                    }
                    anyTransformed = true;
                }
            }

            if (anyTransformed) {
                cu.addImport("com.google.common.collect.*");
                cu.addImport("java.util.*");
                flushes.add(FileFlush.buildLexicalPreserving(cu));
            }
        }

        // write all to file
        if (flushes.size() > 0) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}