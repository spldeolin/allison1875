package com.spldeolin.allison1875.startransformer;

import java.io.File;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.DefaultAstForest;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.MavenProjectClassLoaderUtils;
import com.spldeolin.allison1875.startransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.startransformer.dto.TransformStarChainArgs;
import com.spldeolin.allison1875.startransformer.service.StarChainService;
import com.spldeolin.allison1875.startransformer.service.StarChainTransformerService;
import com.spldeolin.allison1875.startransformer.service.WholeDTOService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Slf4j
public class StarTransformer implements Allison1875MainService {

    @Inject
    private StarChainService starChainService;

    @Inject
    private WholeDTOService wholeDTOService;

    @Inject
    private StarChainTransformerService starChainTransformerService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private Config config;

    @Override
    public void process() {
        // 构造AstForest
        DomainConfig domainConfig = DomainContext.get();
        ClassLoader classLoader = MavenProjectClassLoaderUtils.buildClassLoader(
                new File(domainConfig.getServiceImplModule()), config.getJavaHome());
        AstForestContext.set(new DefaultAstForest(classLoader, domainConfig.getServiceImplSourceRoot().toFile()));

        boolean anyTransformedForAll = false;
        for (CompilationUnit cu : AstForestContext.get()) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);

            for (BlockStmt block : cu.findAll(BlockStmt.class)) {
                for (MethodCallExpr starChain : starChainService.detectStarChains(block)) {

                    // analyze chain
                    ChainAnalysisDTO analysis;
                    try {
                        analysis = starChainService.analyzeStarChain(starChain);
                        log.info("Star Chain analyzed, analysis={}", analysis);
                    } catch (Allison1875Exception e) {
                        log.error("fail to analyze Star Chain, starChain={}", starChain, e);
                        continue;
                    }

                    // generate WholeDTO
                    DataModelGeneration wholeDTOGeneration;
                    try {
                        wholeDTOGeneration = wholeDTOService.generateWholeDTO(analysis);
                        log.info("Whole DTO generated, name={} path={}", wholeDTOGeneration.getDtoName(),
                                wholeDTOGeneration.getPath());
                    } catch (Exception e) {
                        log.error("fail to generate Whole DTO, analysis={}", analysis, e);
                        continue;
                    }

                    // transform Query Chain and replace Star Chain
                    TransformStarChainArgs args = new TransformStarChainArgs();
                    args.setBlock(block);
                    args.setAnalysis(analysis);
                    args.setStarChain(starChain);
                    args.setWholeDTOGeneration(wholeDTOGeneration);
                    try {
                        starChainTransformerService.transformStarChain(args);
                        log.info("Star Chain transformed");
                    } catch (Exception e) {
                        log.error("fail to transformStarChain Star Chain, starAnalysis={}", analysis, e);
                    }

                    anyTransformed = true;
                    anyTransformedForAll = true;
                }
            }
            if (anyTransformed) {
                importExprService.extractQualifiedTypeToImport(cu);
                CompilationUnitUtils.writeJava(cu, true);
            }
        }

        if (anyTransformedForAll) {
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}