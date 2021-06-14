package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.HashUtil;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
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
    private FindEntityAndSuperEntityProc findEntityAndSuperEntityProc;

    @Inject
    private TransformParameterProc transformParameterProc;

    @Inject
    private TransformResultProc transformResultProc;

    @Inject
    private ReplaceDesignProc replaceDesignProc;

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (MethodCallExpr chain : detectQueryDesignProc.process(astForest)) {
            ClassOrInterfaceDeclaration design = findDesign(astForest, chain);
            if (design == null) {
                continue;
            }

            DesignMeta designMeta = parseDesignMeta(design);

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
            List<Saves.Replace> replaces = replaceDesignProc
                    .process(designMeta, chainAnalysis, parameterTransformation, resultTransformation);

            // save
            Saves.add(chain.findCompilationUnit().orElseThrow(CuAbsentException::new), replaces);
            Saves.saveAll();
            detected++;
        }

        if (detected == 0) {
            log.warn("no tables detect.");
        }
    }

    private DesignMeta parseDesignMeta(ClassOrInterfaceDeclaration queryDesign) {
        FieldDeclaration queryMetaField = queryDesign.getFieldByName("meta").orElseThrow(IllegalChainException::new);
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
        String hashing = HashUtil.md5(primaryType.toString());

        if (!hashing.equals(hashcode)) {
            throw new IllegalChainException(
                    "modifications exist in Type [" + designQualifier + "], this Design file need to regenerate");
        }

        return designCu.getType(0).asClassOrInterfaceDeclaration();
    }

}