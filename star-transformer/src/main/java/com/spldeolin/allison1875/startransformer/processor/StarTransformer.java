package com.spldeolin.allison1875.startransformer.processor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.FileBackupUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
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
    private DetectStarChainProc detectStarChainProc;

    @Inject
    private AnalyzeChainProc analyzeChainProc;

    @Inject
    private TransformWholeDtoProc transformWholeDtoProc;

    @Inject
    private TransformChainProc transformChainProc;

    @Override
    public void process(AstForest astForest) {
        Map<Path, String> fileNewContents = Maps.newLinkedHashMap();
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
                    fileNewContents.put(Locations.getAbsolutePath(wholeDtoCu), wholeDtoCu.toString());

                    // transform Query Chain and replace Star Chain
                    transformChainProc.transformAndReplaceStar(block, analysis, starChain);

                    // add import
                    cu.addImport(javabeanArg.getPackageName() + "." + javabeanArg.getClassName());
                    cu.addImport(analysis.getCftDesignQualifier());
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        cu.addImport(phrase.getDtDesignQulifier());
                        cu.addImport(phrase.getDtEntityQualifier());
                    }
                    anyTransformed = true;
                }
            }

            if (anyTransformed) {
                cu.addImport("com.google.common.collect.*");
                cu.addImport("java.util.*");
                fileNewContents.put(Locations.getAbsolutePath(cu), LexicalPreservingPrinter.print(cu));
            }
        }

        // write all to file
        if (fileNewContents.size() > 0) {
            fileNewContents.forEach((path, content) -> {
                FileBackupUtils.backup(path);
                try {
                    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.error("FileUtils#writeStringToFile", e);
                }
            });
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}