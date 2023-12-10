package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.exception.IllegalDesignException;
import com.spldeolin.allison1875.persistencegenerator.facade.exception.SameNameTerminationMethodException;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Log4j2
public class QueryTransformer implements Allison1875MainProcessor {

    @Inject
    private DetectQueryChainProc detectQueryChain;

    @Inject
    private AnalyzeChainProc analyzeChainProc;

    @Inject
    private GenerateMethodXmlProc generateMapperXmlQueryMethodProc;

    @Inject
    private GenerateMethodSignatureProc createMapperQueryMethodProc;

    @Inject
    private TransformParameterProc transformParameterProc;

    @Inject
    private TransformResultProc transformResultProc;

    @Inject
    private ReplaceDesignProc replaceDesignProc;

    @Inject
    private AppendAutowiredMapperProc appendAutowiredMapperProc;

    @Inject
    private DesignProc designProc;

    @Inject
    private FindMapperProc findMapperProc;

    @Inject
    private OffsetMethodNameProc offsetMethodNameProc;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            LexicalPreservingPrinter.setup(cu);
            boolean anyTransformed = false;

            if (cu.findAll(BlockStmt.class).isEmpty()) {
                continue;
            }

            for (BlockStmt directBlock : cu.findAll(BlockStmt.class, TreeTraversal.POSTORDER)) {
                for (MethodCallExpr chain : detectQueryChain.process(directBlock)) {
                    ClassOrInterfaceDeclaration design;
                    DesignMeta designMeta;
                    try {
                        design = designProc.findDesign(astForest, chain);
                        designMeta = designProc.parseDesignMeta(design);
                    } catch (IllegalDesignException e) {
                        log.error("illegal design: " + e.getMessage());
                        return;
                    } catch (SameNameTerminationMethodException e) {
                        return;
                    }

                    // analyze chain
                    ChainAnalysisDto chainAnalysis = analyzeChainProc.process(chain, design, designMeta);
                    chainAnalysis.setDirectBlock(directBlock);

                    // use offset method naming (if no specified)
                    if (chainAnalysis.getNoSpecifiedMethodName()) {
                        CompilationUnit designCu = offsetMethodNameProc.useOffsetMethod(chainAnalysis, designMeta,
                                design);
                        flushes.add(FileFlush.build(designCu));
                    }

                    // if naming conflict, ignore this Design Chain
                    if (findMapperProc.isMapperMethodPresent(astForest, designMeta, chainAnalysis)) {
                        log.warn("Method naming from [{}] conflict exist in Mapper [{}]", chain.toString(),
                                designMeta.getMapperName());
                        return;
                    }

                    // transform Parameter
                    ParameterTransformationDto parameterTransformation = transformParameterProc.transform(chainAnalysis,
                            designMeta, astForest, flushes);

                    // transform Result Type
                    ResultTransformationDto resultTransformation = transformResultProc.transform(chainAnalysis,
                            designMeta, astForest, flushes);

                    // create Method in Mapper
                    CompilationUnit mapperCu = createMapperQueryMethodProc.process(astForest, designMeta, chainAnalysis,
                            parameterTransformation, resultTransformation);
                    if (mapperCu != null) {
                        flushes.add(FileFlush.build(mapperCu));
                    }

                    // create Method in mapper.xml
                    List<FileFlush> xmlFlushes = generateMapperXmlQueryMethodProc.process(astForest, designMeta,
                            chainAnalysis,
                            parameterTransformation, resultTransformation);
                    flushes.addAll(xmlFlushes);

                    // append autowired mapper
                    appendAutowiredMapperProc.append(chain, designMeta);

                    // transform Method Call and replace Design
                    replaceDesignProc.process(designMeta, chainAnalysis, parameterTransformation, resultTransformation);

                    anyTransformed = true;
                }
            }

            if (anyTransformed) {
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