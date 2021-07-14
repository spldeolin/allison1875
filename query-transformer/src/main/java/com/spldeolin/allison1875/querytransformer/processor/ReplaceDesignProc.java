package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
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

        // ensure import
        chainAnalysis.getChain().findCompilationUnit().ifPresent(cu -> {
            Set<String> imports = Sets.newLinkedHashSet();
            if (parameterTransformation != null) {
                imports.addAll(parameterTransformation.getImports());
            }
            imports.addAll(resultTransformation.getImports());
            imports.add(designMeta.getEntityQualifier());
            imports.add(designMeta.getMapperQualifier());
            imports.add(AnnotationConstant.AUTOWIRED_QUALIFIER);
            StringBuilder appendImports = new StringBuilder();
            for (String anImport : imports) {
                appendImports.append("import ").append(anImport).append(";\n");
            }

            Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
            if (packageDeclaration.isPresent()) {
                String packageCode = TokenRanges.getRawCode(packageDeclaration.get());
                replaces.add(new Replace(packageCode, packageCode + "\n\n" + appendImports));
            } else {
                replaces.add(new Replace(null, appendImports.toString()));
            }
        });

        Statement ancestorStatement = chainAnalysis.getChain().findAncestor(Statement.class)
                .orElseThrow(() -> new RuntimeException("cannot find Expression Stmt"));
        String ancestorStatementCode = TokenRanges.getRawCode(ancestorStatement);
        log.info("ancestorStatement={}", ancestorStatementCode);

        // transform Method Call code
        String mceCode = transformMethodCallProc.methodCallExpr(designMeta, chainAnalysis, parameterTransformation);

        String finalReplacement;
        if (chainAnalysis.getChain().getParentNode().filter(parent -> parent instanceof ExpressionStmt).isPresent()) {
            // parent是ExpressionStmt的情况，例如：Design.query("a").one();，则替换整个ancestorStatement（ExpressionStmt是Statement的一种）
            finalReplacement = String
                    .format("%s %s = %s;", resultTransformation.getResultType(), calcAssignVarName(chainAnalysis),
                            mceCode);

        } else if (chainAnalysis.getChain().getParentNode().filter(parent -> parent instanceof AssignExpr)
                .isPresent()) {
            // parent是AssignExpr的情况，例如：Entity a = Design.query("a").one();，则将chain替换成转化出的mce（chain是mce类型）
            finalReplacement = ancestorStatementCode.replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode);

        } else {
            // 以外的情况，往往是继续调用mce返回值，例如：if (0 == Design.update("a").id(-1).over()) { }，则将chain替换成转化出的mce（chain是mce类型）
            finalReplacement = ancestorStatementCode.replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode);
        }

        // 在ancestorStatement的上方添加argument build代码块（如果需要augument build的话）
        String argumentBuildStmts = transformMethodCallProc.argumentBuildStmts(chainAnalysis, parameterTransformation);
        if (argumentBuildStmts != null) {
            finalReplacement = argumentBuildStmts + "\n" + chainAnalysis.getIndent() + finalReplacement;
        }

        // transform Method Call and Assigned code
//        String statementReplacement;
//        if (resultTransformation.getIsAssigned()) {
//            // replace Method Call
//            statementReplacement = TokenRanges.getRawCode(ancestorStatement)
//                    .replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode);
//        } else {
//            // concat Method Call with Assigned
//            if (chainAnalysis.getChainMethod() == ChainMethodEnum.query) {
//                statementReplacement =
//                        resultTransformation.getResultType() + " " + chainAnalysis.getMethodName() + " = ";
//            } else {
//                statementReplacement =
//                        resultTransformation.getResultType() + " " + chainAnalysis.getMethodName() + "Count = ";
//            }
//            statementReplacement += mceCode;
//        }

        replaces.add(new Replace(ancestorStatementCode, finalReplacement));
        return replaces;
    }

    private String calcAssignVarName(ChainAnalysisDto chainAnalysis) {
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.drop
                || chainAnalysis.getChainMethod() == ChainMethodEnum.update) {
            return chainAnalysis.getMethodName() + "Count";
        }
        return chainAnalysis.getMethodName();
    }

}