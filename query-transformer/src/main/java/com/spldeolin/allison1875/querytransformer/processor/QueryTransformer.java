package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.util.HashUtil;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
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
    private GenerateMapperXmlQueryMethodProc generateMapperXmlQueryMethodProc;

    @Inject
    private GenerateMapperQueryMethodProc createMapperQueryMethodProc;

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (MethodCallExpr chain : detectQueryDesignProc.process(astForest)) {
            ClassOrInterfaceDeclaration design = findDesign(astForest, chain);
            if (design == null) {
                continue;
            }

            DesignMeta designMeta = tryParseQueryMeta(design);
            if (designMeta == null) {
                log.warn("incorrect Design Class");
                continue;
            }

            ChainAnalysisDto chainAnalysis;
            try {
                chainAnalysis = analyzeChainProc.process(chain, design);
            } catch (Exception e) {
                log.warn("incorret chain", e);
                continue;
            }

            // TODO 从AstForest中获取entity和superEntity的coid对象，提供给后续的TransformProc


            String queryMethodName = chainAnalysis.getMethodName();
            Collection<CriterionDto> criterions = chainAnalysis.getCriterions();

            // create queryMethod in mapper
            ClassOrInterfaceDeclaration mapper = createMapperQueryMethodProc
                    .process(astForest, designMeta, queryMethodName, criterions);

            // create queryMethod in mapper.xml
            generateMapperXmlQueryMethodProc.process(astForest, designMeta, queryMethodName, criterions);

            // overwirte service
            MethodCallExpr callQueryMethod = StaticJavaParser.parseExpression(
                    MoreStringUtils.lowerFirstLetter(mapper.getNameAsString()) + "." + queryMethodName + "()")
                    .asMethodCallExpr();
            for (CriterionDto criterion : criterions) {
                VerbEnum operator = VerbEnum.of(criterion.getOperator());
                if (operator == VerbEnum.NOT_NULL || operator == VerbEnum.IS_NULL) {
                    continue;
                }
                callQueryMethod.addArgument(criterion.getArgumentExpr());
            }

            // ensure service import & autowired
//            Node parent = mce.getParentNode().orElseThrow(ParentAbsentException::new);
            chain.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
                if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(mapper.getNameAsString())).isPresent()) {
                    service.getMembers().add(0, StaticJavaParser.parseBodyDeclaration(
                            String.format("@Autowired private %s %s;", mapper.getNameAsString(),
                                    MoreStringUtils.lowerFirstLetter(mapper.getNameAsString()))));
                    Imports.ensureImported(service, designMeta.getMapperQualifier());
                    Imports.ensureImported(service, AnnotationConstant.AUTOWIRED_QUALIFIER);
                }
            });

            CompilationUnit cu = chain.findCompilationUnit().orElseThrow(CuAbsentException::new);
            Saves.add(cu, chain.getTokenRange().orElseThrow(RangeAbsentException::new).toString(),
                    callQueryMethod.toString());
            Saves.saveAll();
            detected++;
        }

        if (detected == 0) {
            log.warn("no tables detect.");
        }
    }

    private DesignMeta tryParseQueryMeta(ClassOrInterfaceDeclaration queryDesign) {
        if (!queryDesign.getFieldByName("meta").isPresent()) {
            log.warn("[{}] 缺少元数据 [String meta]", queryDesign.getNameAsString());
            return null;
        }
        FieldDeclaration queryMetaField = queryDesign.getFieldByName("meta").get();
        String queryMetaJson = JavadocDescriptions.getRaw(queryMetaField).replaceAll("\\r?\\n", "");
        DesignMeta queryMeta = JsonUtils.toObject(queryMetaJson, DesignMeta.class);
        return queryMeta;
    }

    private ClassOrInterfaceDeclaration findDesign(AstForest astForest, MethodCallExpr chain) {
        String designQualifier = chain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
        Path designPath = astForest.getPrimaryJavaRoot()
                .resolve(designQualifier.replace('.', File.separatorChar) + ".java");
        CompilationUnit designCu;
        try {
            designCu = StaticJavaParser.parse(designPath);
        } catch (IOException e) {
            throw new RuntimeException("cannot parse Java code", e);
        }

        List<Comment> orphanComments = designCu.getOrphanComments();
        if (orphanComments.size() < 2 || !orphanComments.get(1).isLineComment()) {
            throw new IllegalChainException("cannot found Design Hashcode");
        }
        String hashcode = orphanComments.get(1).asLineComment().getContent().trim();

        if (!designCu.getPrimaryType().isPresent()) {
            throw new IllegalChainException("cannot found Design Type");
        }
        TypeDeclaration<?> primaryType = designCu.getPrimaryType().get();
        String hashing = HashUtil.md5(primaryType.toString());

        if (!hashing.equals(hashcode)) {
            throw new IllegalChainException("incorrect Hashcode");
        }

        return designCu.getType(0).asClassOrInterfaceDeclaration();
    }

}