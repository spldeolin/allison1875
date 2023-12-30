package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.ImportConstant;
import com.spldeolin.allison1875.base.util.EqualsUtils;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.MapOrMultimapBuiltDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.ReplaceDesignService;
import com.spldeolin.allison1875.querytransformer.service.TransformMethodCallService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-09
 */
@Log4j2
@Singleton
public class ReplaceDesignServiceImpl implements ReplaceDesignService {

    @Inject
    private TransformMethodCallService transformMethodCallService;

    @Override
    public void process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {

        // add imports to cu
        chainAnalysis.getChain().findCompilationUnit().ifPresent(cu -> {
            if (parameterTransformation != null) {
                parameterTransformation.getImports().forEach(cu::addImport);
            }
            resultTransformation.getImports().forEach(cu::addImport);
            cu.addImport(designMeta.getEntityQualifier());
            cu.addImport(designMeta.getMapperQualifier());
            cu.addImport(ImportConstant.SPRING_AUTOWIRED);
            cu.addImport(ImportConstant.JAVA_UTIL);
            cu.addImport(ImportConstant.GOOGLE_COMMON_COLLECTION);
        });

        // build Map  build Multimap
        MapOrMultimapBuiltDto mapOrMultimapBuilt = transformMethodCallService.mapOrMultimapBuildStmts(designMeta,
                chainAnalysis, resultTransformation);

        Statement ancestorStatement = chainAnalysis.getChain().findAncestor(Statement.class)
                .orElseThrow(() -> new RuntimeException("cannot find Expression Stmt"));
        String ancestorStatementCode = TokenRanges.getRawCode(ancestorStatement);
        log.info("ancestorStatement={}", ancestorStatementCode);

        // transform Method Call code
        String mceCode = transformMethodCallService.methodCallExpr(designMeta, chainAnalysis, parameterTransformation);

        List<Statement> replacementStatements = Lists.newArrayList();
        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof ExpressionStmt).isPresent()) {
            // parent是ExpressionStmt的情况，例如：Design.query("a").one();，则替换整个ancestorStatement（ExpressionStmt是Statement的一种）
            replacementStatements.add(StaticJavaParser.parseStatement(
                    resultTransformation.getResultType() + " " + calcAssignVarName(chainAnalysis) + " = " + mceCode
                            + ";"));

        } else if (chainAnalysis.getChain().getParentNode()
                .filter(p -> p instanceof AssignExpr || p instanceof VariableDeclarator).isPresent()) {
            // parent是VariableDeclarator的情况，例如：Entity a = Design.query("a").one();
            // 或是AssignExpr的情况，例如：a = Design.query("a").one();
            // 则将chain替换成转化出的mce（chain是mce类型）
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each,
                    ReturnClassifyEnum.multiEach)) {
                replacementStatements.add(StaticJavaParser.parseStatement(
                        resultTransformation.getResultType() + " " + calcAssignVarName(chainAnalysis) + " = " + mceCode
                                + ";"));
            } else {
                replacementStatements.add(StaticJavaParser.parseStatement(
                        ancestorStatementCode.replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode)));
            }
        } else {
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each,
                    ReturnClassifyEnum.multiEach)) {
                throw new UnsupportedOperationException(
                        "以 each 或 multiEach 为返回值的chain表达式，目前只支持定义在赋值语句中或是单独作为一个表达式的情况，不支持其位于其他表达式中的情况");
            }
            // 以外的情况，往往是继续调用mce返回值，例如：if (0 == Design.update("a").id(-1).over()) { }，则将chain替换成转化出的mce（chain是mce类型）
            replacementStatements.add(StaticJavaParser.parseStatement(
                    ancestorStatementCode.replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode)));
        }

        // 在ancestorStatement的上方添加argument build代码块（如果需要augument build的话）
        List<Statement> argumentBuildStmts = transformMethodCallService.argumentBuildStmts(chainAnalysis,
                parameterTransformation);
        if (argumentBuildStmts != null) {
            replacementStatements.addAll(0, argumentBuildStmts);
        }

        // 在ancestorStatement的下方添加map or multimap build代码块（如果需要mapOrMultimapBuilt的话）
        if (mapOrMultimapBuilt != null) {
            replacementStatements.addAll(mapOrMultimapBuilt.getStatements());
        }

        // replace ancestorStatement to replacementStatements
        NodeList<Statement> directBloackStmts = chainAnalysis.getDirectBlock().getStatements();
        directBloackStmts.addAll(directBloackStmts.indexOf(ancestorStatement), replacementStatements);
        directBloackStmts.remove(ancestorStatement);
    }

    private String calcAssignVarName(ChainAnalysisDto chainAnalysis) {
        if (EqualsUtils.equalsAny(chainAnalysis.getChainMethod(), ChainMethodEnum.drop, ChainMethodEnum.update)) {
            return chainAnalysis.getMethodName() + "Count";
        }
        if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each,
                ReturnClassifyEnum.multiEach)) {
            return chainAnalysis.getMethodName() + "List";
        }
        return chainAnalysis.getMethodName();
    }

}