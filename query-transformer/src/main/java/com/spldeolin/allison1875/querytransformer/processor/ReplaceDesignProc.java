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
import com.spldeolin.allison1875.base.util.EqualsUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.MapOrMultimapBuiltDto;
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

        MapOrMultimapBuiltDto mapOrMultimapBuilt = transformMethodCallProc
                .mapOrMultimapBuildStmts(designMeta, chainAnalysis, resultTransformation);

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
            if (mapOrMultimapBuilt != null) {
                imports.addAll(mapOrMultimapBuilt.getImports());
            }
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
        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof ExpressionStmt).isPresent()) {
            // parent是ExpressionStmt的情况，例如：Design.query("a").one();，则替换整个ancestorStatement（ExpressionStmt是Statement的一种）
            finalReplacement = String
                    .format("%s %s = %s;", resultTransformation.getResultType(), calcAssignVarName(chainAnalysis),
                            mceCode);

        } else if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof AssignExpr).isPresent()) {
            // parent是VariableDeclarator的情况，例如：Entity a = Design.query("a").one();，则将chain替换成转化出的mce（chain是mce类型）
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each,
                    ReturnClassifyEnum.multiEach)) {
                finalReplacement = String
                        .format("%s %s = %s;", resultTransformation.getResultType(), calcAssignVarName(chainAnalysis),
                                mceCode);
            } else {
                finalReplacement = ancestorStatementCode
                        .replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode);
            }
        } else {
            if (EqualsUtils.equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each,
                    ReturnClassifyEnum.multiEach)) {
                throw new UnsupportedOperationException(
                        "以 each 或 multiEach 为返回值的chain表达式，目前只支持定义在赋值语句中或是单独作为一个表达式的情况，不支持其位于其他表达式中的情况");
            }
            // 以外的情况，往往是继续调用mce返回值，例如：if (0 == Design.update("a").id(-1).over()) { }，则将chain替换成转化出的mce（chain是mce类型）
            finalReplacement = ancestorStatementCode.replace(TokenRanges.getRawCode(chainAnalysis.getChain()), mceCode);
        }

        // 在ancestorStatement的上方添加argument build代码块（如果需要augument build的话）
        String argumentBuildStmts = transformMethodCallProc.argumentBuildStmts(chainAnalysis, parameterTransformation);
        if (argumentBuildStmts != null) {
            finalReplacement = argumentBuildStmts + "\n" + chainAnalysis.getIndent() + finalReplacement;
        }

        // 在ancestorStatement的下方添加map or multimap build代码块（如果需要augument build的话）
        if (mapOrMultimapBuilt != null) {
            finalReplacement = finalReplacement + "\n" + chainAnalysis.getIndent() + mapOrMultimapBuilt.getCode();
        }

        replaces.add(new Replace(ancestorStatementCode, finalReplacement));
        return replaces;
    }

    private String calcAssignVarName(ChainAnalysisDto chainAnalysis) {
        if (EqualsUtils.equalsAny(chainAnalysis.getChainMethod(), ChainMethodEnum.drop, ChainMethodEnum.update)) {
            return chainAnalysis.getMethodName() + "Count";
        }
        if (EqualsUtils
                .equalsAny(chainAnalysis.getReturnClassify(), ReturnClassifyEnum.each, ReturnClassifyEnum.multiEach)) {
            return chainAnalysis.getMethodName() + "List";
        }
        return chainAnalysis.getMethodName();
    }

}