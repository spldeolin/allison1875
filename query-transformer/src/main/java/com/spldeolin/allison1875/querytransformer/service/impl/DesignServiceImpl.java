package com.spldeolin.allison1875.querytransformer.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.ProceedingAstForest;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.config.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.dto.ReplaceDesignArgs;
import com.spldeolin.allison1875.querytransformer.enums.ReturnStyleEnum;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.TransformMethodCallService;
import com.spldeolin.allison1875.querytransformer.util.TokenRangeUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-07-01
 */
@Slf4j
public class DesignServiceImpl implements DesignService {

    @Inject
    private TransformMethodCallService transformMethodCallService;

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    @Override
    public ClassOrInterfaceDeclaration findCoidWithChecksum(String qualifier) {
        Optional<CompilationUnit> opt;
        if (AstForestContext.get() instanceof ProceedingAstForest) {
            opt = AstForestContext.get().tryFindCu(qualifier);
        } else {
            Path sourceRoot = Optional.ofNullable(queryTransformerConfig.getPersistenceSourcePath()).map(File::toPath)
                    .orElse(AstForestContext.get().getSourceRoot());
            opt = CompilationUnitUtils.tryFindCu(sourceRoot, qualifier);
        }

        if (!opt.isPresent()) {
            throw new Allison1875Exception("cannot found Design [" + qualifier + "]");
        }
        CompilationUnit designCu = opt.get();

        List<Comment> orphanComments = designCu.getOrphanComments();
        if (orphanComments.size() < 2 || !orphanComments.get(1).isLineComment()) {
            throw new Allison1875Exception("cannot found Design Hashcode");
        }
        String hashcode = orphanComments.get(1).asLineComment().getContent().trim();

        if (!designCu.getPrimaryType().isPresent()) {
            throw new Allison1875Exception(
                    "cannot found Design Type in file [" + CompilationUnitUtils.getCuAbsolutePath(designCu)
                            + "], this Design file need to regenerate");
        }
        TypeDeclaration<?> primaryType = designCu.getPrimaryType().get();
        String hashing = HashingUtils.hashTypeDeclaration(primaryType);

        if (!hashing.equals(hashcode)) {
            throw new Allison1875Exception(
                    "modifications exist in Type [" + qualifier + "], this Design file need to regenerate");
        }

        return designCu.getType(0).asClassOrInterfaceDeclaration();
    }

    @Override
    public DesignMetaDTO findDesignMeta(MethodCallExpr designChain) {
        String designQualifier = designChain.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
        ClassOrInterfaceDeclaration design = this.findCoidWithChecksum(designQualifier);
        return findDesignMeta(design);
    }

    @Override
    public DesignMetaDTO findDesignMeta(ClassOrInterfaceDeclaration design) {
        FieldDeclaration queryMetaField = design.getFieldByName(KeywordConstant.META_FIELD_NAME).orElseThrow(
                () -> new Allison1875Exception("Meta Field is not exist in Design [" + design.getNameAsString() + "]"));
        if (CollectionUtils.isEmpty(queryMetaField.getVariables())) {
            throw new Allison1875Exception("Variable is not exist in Meta Field, metaField=" + queryMetaField);
        }
        Expression initializer = queryMetaField.getVariable(0).getInitializer().orElseThrow(
                () -> new Allison1875Exception("Initializer is not exist in Meta Field, metaField=" + queryMetaField));
        String metaJson = StringEscapeUtils.unescapeJava(initializer.asStringLiteralExpr().getValue());
        return JsonUtils.toObject(metaJson, DesignMetaDTO.class);
    }

    @Override
    public void replaceDesign(ReplaceDesignArgs args) {
        ChainAnalysisDTO chainAnalysis = args.getChainAnalysis();
        GenerateParamRetval generateParamRetval = args.getGenerateParamRetval();
        GenerateReturnTypeRetval generateReturnTypeRetval = args.getGenerateReturnTypeRetval();

        Statement ancestorStatement = chainAnalysis.getChain().findAncestor(Statement.class)
                .orElseThrow(() -> new Allison1875Exception("cannot find parent for" + chainAnalysis.getChain()));
        String ancestorStatementCode = TokenRangeUtils.getRawCode(ancestorStatement);
        log.info("ancestorStatement={}", ancestorStatementCode);

        // transform Method Call code
        String mceCode = transformMethodCallService.methodCallExpr(args.getMapperVarName(), chainAnalysis,
                generateParamRetval);

        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.GROUP) {
            String groupKeyName = chainAnalysis.getMapOrGroupKeyProperty().getPropertyName();
            mceCode += String.format(".stream().collect(java.util.stream.Collectors.groupingBy(%s::get%s))",
                    MoreStringUtils.splitAndGetLastPart(args.getGenerateReturnTypeRetval().getElementTypeQualifier(),
                            "."), StringUtils.capitalize(groupKeyName));
        }

        List<Statement> replacementStatements = Lists.newArrayList();

        // 分页
        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
            MethodCallExpr callCountMce = StaticJavaParser.parseExpression(mceCode).asMethodCallExpr();
            callCountMce.setName(chainAnalysis.getCountMethodNameForPage());
            replacementStatements.add(StaticJavaParser.parseStatement(
                    "long " + calcAssignVarName(chainAnalysis) + "Total = " + callCountMce + ";"));
        }

        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof ExpressionStmt).isPresent()) {
            // parent是ExpressionStmt的情况，例如：Design.query("a").one();，则替换整个ancestorStatement（ExpressionStmt是Statement的一种）
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.GROUP) {
                String propertyTypeName = chainAnalysis.getMapOrGroupKeyProperty().getJavaType().getQualifier();
                replacementStatements.add(StaticJavaParser.parseStatement(
                        "java.util.Map<" + propertyTypeName + ", java.util.List<"
                                + generateReturnTypeRetval.getElementTypeQualifier() + ">> " + calcAssignVarName(
                                chainAnalysis) + " = " + mceCode + ";"));
            } else {
                replacementStatements.add(StaticJavaParser.parseStatement(
                        generateReturnTypeRetval.getResultType() + " " + calcAssignVarName(chainAnalysis) + " = "
                                + mceCode + ";"));
            }
        } else if (chainAnalysis.getChain().getParentNode()
                .filter(p -> p instanceof AssignExpr || p instanceof VariableDeclarator).isPresent()) {
            // parent是VariableDeclarator的情况，例如：Entity a = Design.query("a").one();
            // 或是AssignExpr的情况，例如：a = Design.query("a").one();
            // 则将chain替换成转化出的mce（chain是mce类型）
            replacementStatements.add(StaticJavaParser.parseStatement(
                    ancestorStatementCode.replace(TokenRangeUtils.getRawCode(chainAnalysis.getChain()), mceCode)));
        } else {
            // 以外的情况，往往是继续调用mce返回值，例如：if (0 == Design.update("a").id(-1).over()) { }，则将chain替换成转化出的mce（chain是mce类型）
            replacementStatements.add(StaticJavaParser.parseStatement(
                    ancestorStatementCode.replace(TokenRangeUtils.getRawCode(chainAnalysis.getChain()), mceCode)));
        }

        // 在ancestorStatement的上方添加argument build代码块（如果需要augument build的话）
        if (generateParamRetval.getIsParamDTO()) {
            List<Statement> argumentBuildStmts = transformMethodCallService.argumentBuildStmts(chainAnalysis,
                    generateParamRetval);
            replacementStatements.addAll(0, argumentBuildStmts);
        }

        // replace ancestorStatement to replacementStatements
        NodeList<Statement> directBloackStmts = chainAnalysis.getDirectBlock().getStatements();
        directBloackStmts.addAll(directBloackStmts.indexOf(ancestorStatement), replacementStatements);
        directBloackStmts.remove(ancestorStatement);
    }

    private String calcAssignVarName(ChainAnalysisDTO chainAnalysis) {
        if (Lists.newArrayList(KeywordConstant.ChainInitialMethod.DELETE, KeywordConstant.ChainInitialMethod.UPDATE)
                .contains(chainAnalysis.getChainInitialMethod())) {
            return chainAnalysis.getMethodName() + "Count";
        }
        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.GROUP) {
            return chainAnalysis.getMethodName() + "List";
        }
        return chainAnalysis.getMethodName();
    }

}