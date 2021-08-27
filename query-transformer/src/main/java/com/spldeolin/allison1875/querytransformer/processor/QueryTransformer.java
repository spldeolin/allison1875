package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.exception.IllegalDesignException;
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

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (CompilationUnit cu : astForest) {

            // collect replace codes
            List<Replace> replaces = Lists.newArrayList();

            // append needed mapper
            Collection<Triple<MethodCallExpr, ClassOrInterfaceDeclaration, DesignMeta>> chain2DesignMeta =
                    Lists.newArrayList();
            List<MethodCallExpr> chains = detectQueryDesignProc.process(cu);
            Set<String> autowiredMappers = Sets.newHashSet();
            for (MethodCallExpr chain : chains) {
                ClassOrInterfaceDeclaration design;
                DesignMeta designMeta;
                try {
                    design = designProc.findDesign(astForest, chain);
                    designMeta = designProc.parseDesignMeta(design);
                } catch (IllegalDesignException e) {
                    log.error("illegal design: " + e.getMessage());
                    detected++;
                    continue;
                }
                chain2DesignMeta.add(Triple.of(chain, design, designMeta));

                appendAutowiredMapperProc.append(autowiredMappers, replaces, chain, designMeta);
            }

            // resolve chain
            for (Triple<MethodCallExpr, ClassOrInterfaceDeclaration, DesignMeta> triple : chain2DesignMeta) {
                MethodCallExpr chain = triple.getLeft();
                ClassOrInterfaceDeclaration design = triple.getMiddle();
                DesignMeta designMeta = triple.getRight();

                // analyze chain
                ChainAnalysisDto chainAnalysis = analyzeChainProc.process(chain, design, designMeta);

                if (findMapperProc.isMapperMethodPresent(astForest, designMeta, chainAnalysis)) {
                    log.warn("Method [{}] naming conflict exist in Mapper [{}]", chainAnalysis.getMethodName(),
                            designMeta.getMapperName());
                    continue;
                }

                // transform Parameter
                ParameterTransformationDto parameterTransformation = transformParameterProc.transform(chainAnalysis,
                        designMeta, astForest);

                // transform Result Type
                ResultTransformationDto resultTransformation = transformResultProc.transform(chainAnalysis, designMeta,
                        astForest);

                // create Method in Mapper
                createMapperQueryMethodProc.process(astForest, designMeta, chainAnalysis, parameterTransformation,
                        resultTransformation);

                // create Method in mapper.xml
                generateMapperXmlQueryMethodProc.process(astForest, designMeta, chainAnalysis, parameterTransformation,
                        resultTransformation);

                // transform Method Call and replace Design
                replaces.addAll(replaceDesignProc.process(designMeta, chainAnalysis, parameterTransformation,
                        resultTransformation));

                detected++;
                Saves.add(cu, replaces);
                Saves.saveAll();
            }
        }

        if (detected == 0) {
            log.warn("no Chain detected");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

}