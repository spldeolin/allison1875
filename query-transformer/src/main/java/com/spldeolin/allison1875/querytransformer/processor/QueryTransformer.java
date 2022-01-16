package com.spldeolin.allison1875.querytransformer.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
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
    private DetectQueryDesignProc detectQueryDesignProc;

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

    private static final AtomicInteger detected = new AtomicInteger(0);

    @Override
    public void process(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            tryDetectAndTransform(astForest, cu);
        }
        if (detected.get() == 0) {
            log.warn("no valid Chain detected");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private void tryDetectAndTransform(AstForest astForest, CompilationUnit cu) {
        MethodCallExpr chain = detectQueryDesignProc.processFirst(cu);
        if (chain == null) {
            return;
        }

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

        // use offset method naming (if no specified)
        if (chainAnalysis.getNoSpecifiedMethodName()) {
            offsetMethodNameProc.useOffsetMethod(chainAnalysis, designMeta, design);
        }

        // if naming conflict, ignore this Design Chain
        if (findMapperProc.isMapperMethodPresent(astForest, designMeta, chainAnalysis)) {
            log.warn("Method naming from [{}] conflict exist in Mapper [{}]", chain.toString(),
                    designMeta.getMapperName());
            return;
        }

        // transform Parameter
        ParameterTransformationDto parameterTransformation = transformParameterProc.transform(chainAnalysis, designMeta,
                astForest);

        // transform Result Type
        ResultTransformationDto resultTransformation = transformResultProc.transform(chainAnalysis, designMeta,
                astForest);

        // create Method in Mapper
        createMapperQueryMethodProc.process(astForest, designMeta, chainAnalysis, parameterTransformation,
                resultTransformation);

        // create Method in mapper.xml
        generateMapperXmlQueryMethodProc.process(astForest, designMeta, chainAnalysis, parameterTransformation,
                resultTransformation);

        // append autowired mapper
        List<Replace> replaces = Lists.newArrayList();
        replaces.addAll(appendAutowiredMapperProc.append(chain, designMeta));

        // transform Method Call and replace Design
        replaces.addAll(
                replaceDesignProc.process(designMeta, chainAnalysis, parameterTransformation, resultTransformation));
        Saves.add(cu, replaces);

        Saves.saveAll();
        detected.addAndGet(1);

        Path javaPath = Locations.getAbsolutePath(cu);
        try {
            cu = StaticJavaParser.parse(javaPath);
        } catch (IOException e) {
            log.warn("SourceCode parse unsuccessfully [{}]", javaPath, e);
            return;
        }
        this.tryDetectAndTransform(astForest, cu);
    }

}