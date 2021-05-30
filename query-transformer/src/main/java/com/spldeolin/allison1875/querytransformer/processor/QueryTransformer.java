package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
import com.spldeolin.allison1875.querytransformer.javabean.AnalyzeCriterionResultDto;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;
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
    private AnalyzeCriterionProc analyzeSqlTokenProc;

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

            QueryMeta queryMeta = tryParseQueryMeta(design);
            if (queryMeta == null) {
                continue;
            }

            AnalyzeCriterionResultDto analyzeCriterionResult = analyzeSqlTokenProc.process(chain, queryMeta, design);
            String queryMethodName = analyzeCriterionResult.getQueryMethodName();
            Collection<CriterionDto> criterions = analyzeCriterionResult.getCriterions();

            // create queryMethod in mapper
            ClassOrInterfaceDeclaration mapper = createMapperQueryMethodProc
                    .process(astForest, queryMeta, queryMethodName, criterions);

            // create queryMethod in mapper.xml
            generateMapperXmlQueryMethodProc.process(astForest, queryMeta, queryMethodName, criterions);

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
                    Imports.ensureImported(service, queryMeta.getMapperQualifier());
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

    private QueryMeta tryParseQueryMeta(ClassOrInterfaceDeclaration queryDesign) {
        if (!queryDesign.getFieldByName("meta").isPresent()) {
            log.warn("[{}] 缺少元数据 [String meta]", queryDesign.getNameAsString());
            return null;
        }
        FieldDeclaration queryMetaField = queryDesign.getFieldByName("meta").get();
        String queryMetaJson = JavadocDescriptions.getRaw(queryMetaField).replaceAll("\\r?\\n", "");
        QueryMeta queryMeta = JsonUtils.toObject(queryMetaJson, QueryMeta.class);
        return queryMeta;
    }

    private ClassOrInterfaceDeclaration findDesign(AstForest astForest, MethodCallExpr chain) {
        ClassOrInterfaceDeclaration design;
        try {
            String designQualifier = chain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
            Path designPath = astForest.getPrimaryJavaRoot()
                    .resolve(designQualifier.replace('.', File.separatorChar) + ".java");
            CompilationUnit designCu = StaticJavaParser.parse(designPath);
            design = designCu.getType(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("Design编写方式不正确", e);
            return null;
        }
        return design;
    }

}