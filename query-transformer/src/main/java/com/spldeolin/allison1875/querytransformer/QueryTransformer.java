package com.spldeolin.allison1875.querytransformer;

import java.util.List;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.javabean.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.exception.IllegalDesignException;
import com.spldeolin.allison1875.querytransformer.exception.SameNameTerminationMethodException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateMethodToMapperXmlArgs;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.javabean.ReplaceDesignArgs;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerService;
import com.spldeolin.allison1875.querytransformer.service.MethodGeneratorService;
import com.spldeolin.allison1875.querytransformer.service.QueryChainAnalyzerService;
import com.spldeolin.allison1875.querytransformer.service.QueryChainDetectorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Slf4j
public class QueryTransformer implements Allison1875MainService {

    @Inject
    private QueryChainDetectorService queryChainDetectorService;

    @Inject
    private QueryChainAnalyzerService queryChainAnalyzerService;

    @Inject
    private MapperLayerService mapperLayerService;

    @Inject
    private MethodGeneratorService methodGeneratorService;

    @Inject
    private DesignService designService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private MemberAdderService memberAdderService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;
            LexicalPreservingPrinter.setup(cu);

            if (CollectionUtils.isEmpty(cu.findAll(BlockStmt.class))) {
                continue;
            }
            for (BlockStmt directBlock : cu.findAll(BlockStmt.class, TreeTraversal.POSTORDER)) {
                for (MethodCallExpr queryChain : queryChainDetectorService.detectQueryChains(directBlock)) {

                    if (!queryChain.findAncestor(ClassOrInterfaceDeclaration.class).isPresent()) {
                        log.warn("Query Chain is not in a Coid, ignore, queryChain={}", queryChain);
                        continue;
                    }
                    ClassOrInterfaceDeclaration directCoid = queryChain.findAncestor(ClassOrInterfaceDeclaration.class)
                            .get();

                    ClassOrInterfaceDeclaration design;
                    DesignMetaDto designMeta;
                    try {
                        design = designService.detectDesign(astForest, queryChain);
                        designMeta = designService.analyzeDesignMeta(design);
                        log.info("Design found and Design Meta parsed, designName={} designMeta={}", design.getName(),
                                designMeta);
                    } catch (IllegalDesignException e) {
                        log.error("fail to find Design or parse Design Meta, queryChain={}", queryChain, e);
                        continue;
                    } catch (SameNameTerminationMethodException e) {
                        continue;
                    }

                    // analyze queryChain
                    ChainAnalysisDto chainAnalysis;
                    try {
                        chainAnalysis = queryChainAnalyzerService.analyzeQueryChain(queryChain, design, designMeta);
                        log.info("Query Chain analyzed, chainAnalysis={}", chainAnalysis);
                    } catch (IllegalChainException e) {
                        log.error("fail to analyze Query Chain, queryChain={}", queryChain, e);
                        continue;
                    }
                    chainAnalysis.setDirectBlock(directBlock);

                    // generate Parameter
                    GenerateParamRetval generateParamRetval;
                    try {
                        generateParamRetval = methodGeneratorService.generateParam(chainAnalysis, designMeta,
                                astForest);
                        log.info("Param generated, retval={}", generateParamRetval);
                    } catch (Exception e) {
                        log.error("fail to generate param chainAnalysis={} designMeta={}", chainAnalysis, designMeta,
                                e);
                        continue;
                    }

                    if (generateParamRetval.getCondFlush() != null) {
                        flushes.add(generateParamRetval.getCondFlush());
                    }

                    // generate Result Type
                    GenerateReturnTypeRetval generateReturnTypeRetval;
                    try {
                        generateReturnTypeRetval = methodGeneratorService.generateReturnType(chainAnalysis, designMeta,
                                astForest);
                        log.info("Result generated, generation={}", generateReturnTypeRetval);
                    } catch (Exception e) {
                        log.error("fail to generate result chainAnalysis={} designMeta={}", chainAnalysis, designMeta,
                                e);
                        continue;
                    }
                    if (generateReturnTypeRetval.getFlush() != null) {
                        flushes.add(generateReturnTypeRetval.getFlush());
                    }

                    // generate Method to Mapper
                    GenerateMethodToMapperArgs gmtmArgs = new GenerateMethodToMapperArgs();
                    gmtmArgs.setAstForest(astForest);
                    gmtmArgs.setDesignMeta(designMeta);
                    gmtmArgs.setChainAnalysis(chainAnalysis);
                    gmtmArgs.setCloneParameters(generateParamRetval.getParameters().stream().map(Parameter::clone)
                            .collect(Collectors.toList()));
                    gmtmArgs.setClonedReturnType(generateReturnTypeRetval.getResultType().clone());
                    mapperLayerService.generateMethodToMapper(gmtmArgs).ifPresent(flushes::add);

                    // generate Method into mapper.xml
                    GenerateMethodToMapperXmlArgs gmtmxArgs = new GenerateMethodToMapperXmlArgs();
                    gmtmxArgs.setAstForest(astForest);
                    gmtmxArgs.setDesignMeta(designMeta);
                    gmtmxArgs.setChainAnalysis(chainAnalysis);
                    gmtmxArgs.setGenerateParamRetval(generateParamRetval);
                    gmtmxArgs.setGenerateReturnTypeRetval(generateReturnTypeRetval);
                    List<FileFlush> xmlFlushes = mapperLayerService.generateMethodToMapperXml(gmtmxArgs);
                    flushes.addAll(xmlFlushes);

                    // append autowired mapper
                    AddInjectFieldRetval addInjectFieldRetval = memberAdderService.addInjectField(
                            designMeta.getMapperQualifier(), MoreStringUtils.toLowerCamel(designMeta.getMapperName()),
                            directCoid);

                    // transform Query Design
                    ReplaceDesignArgs args = new ReplaceDesignArgs();
                    args.setDesignMeta(designMeta);
                    args.setChainAnalysis(chainAnalysis);
                    args.setGenerateParamRetval(generateParamRetval);
                    args.setGenerateReturnTypeRetval(generateReturnTypeRetval);
                    args.setMapperVarName(addInjectFieldRetval.getFieldVarName());
                    designService.replaceDesign(args);

                    anyTransformed = true;
                }
            }
            if (anyTransformed) {
                importExprService.extractQualifiedTypeToImport(cu);
                flushes.add(FileFlush.buildLexicalPreserving(cu));
            }
        }

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}