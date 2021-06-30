package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.HashUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
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

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (CompilationUnit cu : astForest) {

            // collect replace codes
            List<Replace> replaces = Lists.newArrayList();

            // append needed mapper
            Collection<Triple<MethodCallExpr, ClassOrInterfaceDeclaration, DesignMeta>> chain2DesignMeta = Lists
                    .newArrayList();
            List<MethodCallExpr> chains = detectQueryDesignProc.process(cu);
            Set<String> autowiredMappers = Sets.newHashSet();
            for (MethodCallExpr chain : chains) {
                ClassOrInterfaceDeclaration design = findDesign(astForest, chain);
                if (design == null) {
                    continue;
                }
                DesignMeta designMeta = parseDesignMeta(design);
                chain2DesignMeta.add(Triple.of(chain, design, designMeta));

                appendAutowiredMapperProc.append(autowiredMappers, replaces, chain, designMeta);
            }

            // resolve chain
            for (Triple<MethodCallExpr, ClassOrInterfaceDeclaration, DesignMeta> triple : chain2DesignMeta) {
                MethodCallExpr chain = triple.getLeft();
                ClassOrInterfaceDeclaration design = triple.getMiddle();
                DesignMeta designMeta = triple.getRight();

                ChainAnalysisDto chainAnalysis = analyzeChainProc.process(chain, design);

                // transform Parameter
                ParameterTransformationDto parameterTransformation = transformParameterProc
                        .transform(chainAnalysis, designMeta, astForest);

                // transform Result Type
                ResultTransformationDto resultTransformation = transformResultProc
                        .transform(chainAnalysis, designMeta, astForest);

                // create Method in Mapper
                createMapperQueryMethodProc
                        .process(astForest, designMeta, chainAnalysis, parameterTransformation, resultTransformation);

                // create Method in mapper.xml
                generateMapperXmlQueryMethodProc
                        .process(astForest, designMeta, chainAnalysis, parameterTransformation, resultTransformation);

                // transform Method Call and replace Design
                replaces.addAll(replaceDesignProc
                        .process(designMeta, chainAnalysis, parameterTransformation, resultTransformation));

                detected++;
                Saves.add(cu, replaces);
                Saves.saveAll();
            }
        }

        if (detected == 0) {
            log.warn("no tables detect.");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private DesignMeta parseDesignMeta(ClassOrInterfaceDeclaration queryDesign) {
        FieldDeclaration queryMetaField = queryDesign.getFieldByName(TokenWordConstant.META_FIELD_NAME)
                .orElseThrow(IllegalChainException::new);
        Expression initializer = queryMetaField.getVariable(0).getInitializer().orElseThrow(IllegalChainException::new);
        String metaJson = StringEscapeUtils.unescapeJava(initializer.asStringLiteralExpr().getValue());
        return JsonUtils.toObject(metaJson, DesignMeta.class);
    }

    private ClassOrInterfaceDeclaration findDesign(AstForest astForest, MethodCallExpr chain) {
        String designQualifier = chain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
        CompilationUnit designCu = astForest.findCu(designQualifier);

        List<Comment> orphanComments = designCu.getOrphanComments();
        if (orphanComments.size() < 2 || !orphanComments.get(1).isLineComment()) {
            throw new IllegalChainException("cannot found Design Hashcode");
        }
        String hashcode = orphanComments.get(1).asLineComment().getContent().trim();

        if (!designCu.getPrimaryType().isPresent()) {
            throw new IllegalChainException(
                    "cannot found Design Type in file [" + Locations.getStorage(designCu).getFileName()
                            + "], this Design file need to regenerate");
        }
        TypeDeclaration<?> primaryType = designCu.getPrimaryType().get();
        String hashing = HashUtils.md5(primaryType.toString());

        if (!hashing.equals(hashcode)) {
            throw new IllegalChainException(
                    "modifications exist in Type [" + designQualifier + "], this Design file need to regenerate");
        }

        return designCu.getType(0).asClassOrInterfaceDeclaration();
    }

}