package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.ParentAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.querytransformer.enums.OperatorEnum;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;

/**
 * @author Deolin 2020-10-06
 */
public class QueryTransformer implements Allison1875MainProcessor {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(QueryTransformer.class);

    @Override
    public void process(AstForest astForest) {
        DetectQueryDesignProc detectQueryDesignProc = new DetectQueryDesignProc(astForest, "over").process();
        for (MethodCallExpr mce : detectQueryDesignProc.getMces()) {
            CompilationUnit cu = mce.findCompilationUnit().orElseThrow(CuAbsentException::new);
            Node parent = mce.getParentNode().orElseThrow(ParentAbsentException::new);

            ClassOrInterfaceDeclaration queryDesign = findQueryDesign(cu, mce);
            if (queryDesign == null) {
                continue;
            }

            QueryMeta queryMeta = JsonUtils.toObject(
                    queryDesign.getOrphanComments().get(0).getContent().replaceAll("\\r?\\n", "").replaceAll(" ", ""),
                    QueryMeta.class);

            AnalyzeCriterionProc analyzeSqlTokenProc = new AnalyzeCriterionProc(mce, queryMeta).process();
            String queryMethodName = analyzeSqlTokenProc.getQueryMethodName();
            Collection<CriterionDto> criterions = analyzeSqlTokenProc.getCriterions();


            // create queryMethod in mapper
            GenerateMapperQueryMethodProc createMapperQueryMethodProc = new GenerateMapperQueryMethodProc(cu, queryMeta,
                    queryMethodName, criterions).process();
            ClassOrInterfaceDeclaration mapper = createMapperQueryMethodProc.getMapper();

            // create queryMethod in mapper.xml
            new GenerateMapperXmlQueryMethodProc(astForest, queryMeta, queryMethodName, criterions).process();

            // overwirte service
            MethodCallExpr callQueryMethod = StaticJavaParser.parseExpression(
                    StringUtils.lowerFirstLetter(mapper.getNameAsString()) + "." + queryMethodName + "()")
                    .asMethodCallExpr();
            for (CriterionDto criterion : criterions) {
                OperatorEnum operator = OperatorEnum.of(criterion.getOperator());
                if (operator == OperatorEnum.NOT_NULL || operator == OperatorEnum.IS_NULL) {
                    continue;
                }
                callQueryMethod.addArgument(criterion.getArgumentExpr());
            }
            parent.replace(mce, callQueryMethod);

            // ensure service import & autowired
            parent.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
                if (!service.getFieldByName(StringUtils.lowerFirstLetter(mapper.getNameAsString())).isPresent()) {
                    service.getMembers().add(0, StaticJavaParser.parseBodyDeclaration(
                            String.format("@Autowired private %s %s;", mapper.getNameAsString(),
                                    StringUtils.lowerFirstLetter(mapper.getNameAsString()))));
                    Imports.ensureImported(service, queryMeta.getMapperQualifier());
                    Imports.ensureImported(service, "org.springframework.beans.factory.annotation.Autowired");
                }
            });

            Saves.save(cu);
        }
    }

    private ClassOrInterfaceDeclaration findQueryDesign(CompilationUnit cu, MethodCallExpr mce) {
        ClassOrInterfaceDeclaration queryDesign;
        try {
            String queryDesignQualifier = mce.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
            Path queryDesignPath = Locations.getStorage(cu).getSourceRoot()
                    .resolve(queryDesignQualifier.replace('.', File.separatorChar) + ".java");
            CompilationUnit queryDesignCu = StaticJavaParser.parse(queryDesignPath);
            queryDesign = queryDesignCu.getType(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("QueryDesign编写方式不正确", e);
            return null;
        }
        return queryDesign;
    }

}