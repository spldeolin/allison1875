package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-09
 */
@Log4j2
@Singleton
public class ReplaceDesignProc {

    @Inject
    private TransformMethodCallProc transformMethodCallProc;

    public List<Saves.Replace> process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        List<Saves.Replace> replaces = Lists.newArrayList();

        // ensure service import & autowired
        String autowiredField = String.format("@Autowired private %s %s;", designMeta.getMapperName(),
                MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()));
        chainAnalysis.getChain().findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
            if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(designMeta.getMapperName())).isPresent()) {
                List<FieldDeclaration> fields = service.getFields();
                if (fields.size() > 0) {
                    String lastFieldCode = TokenRanges.getRawCode(Iterables.getLast(fields));
                    replaces.add(new Replace(lastFieldCode, lastFieldCode + autowiredField));
                } else {
                    List<MethodDeclaration> methods = service.getMethods();
                    if (methods.size() > 0) {
                        String firstMethodCode = TokenRanges.getRawCode(methods.get(0));
                        replaces.add(new Replace(firstMethodCode, autowiredField + firstMethodCode));
                    }
                }
            }
        });

        ExpressionStmt exprStmt = chainAnalysis.getChain().findAncestor(ExpressionStmt.class)
                .orElseThrow(() -> new RuntimeException("cannot find Expression Stmt"));
        Expression chainExpr = exprStmt.getExpression();
        log.info("chainExpr={}", chainExpr);

        // overwirte methodCall
        String chainReplacement = transformMethodCallProc
                .process(designMeta, chainAnalysis, parameterTransformation, resultTransformation);
        String chainExprReplacement = chainAnalysis.getIndent() + TokenRanges.getRawCode(chainExpr)
                .replace(TokenRanges.getRawCode(chainAnalysis.getChain()), chainReplacement);

        String argumentBuild = transformMethodCallProc.argumentBuild(chainAnalysis, parameterTransformation);
        if (argumentBuild != null) {
            chainExprReplacement = argumentBuild + "\n" + chainExprReplacement;
        }

        replaces.add(new Replace(TokenRanges.getRawCode(chainExpr), chainExprReplacement));

        return replaces;
    }

}