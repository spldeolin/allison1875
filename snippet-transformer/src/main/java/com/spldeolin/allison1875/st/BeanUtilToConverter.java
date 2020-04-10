package com.spldeolin.allison1875.st;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.st.common.CuCreator;
import com.spldeolin.allison1875.st.common.CuCreator.TypeCreator;
import lombok.extern.log4j.Log4j2;

/**
 * 将所有的BeanCopyUtil.copy(src, dest);替换为new SrcTypeToDestTypeConverter().convert(src, dest);，
 * 并新建SrcTypeToDestTypeConverter类
 *
 * @author Deolin 2020-01-27
 */
@Log4j2
public class BeanUtilToConverter {

    public static void main(String[] args) {
        StaticJavaParser.getConfiguration().setAttributeComments(false);

        Collection<CompilationUnit> creations = Lists.newLinkedList();
        Collection<CompilationUnit> updates = Lists.newLinkedList();
        StaticAstContainer.getCompilationUnits().forEach(cu -> {
            MutableBoolean hasSetup = new MutableBoolean(false);
            cu.findAll(MethodCallExpr.class).forEach(
                    methodCallExpr -> methodCallExpr.getScope().ifPresent(scope -> scope.ifNameExpr(scopeName -> {
                        if ("BeanCopyUtil".equals(scopeName.getNameAsString()) && "copy"
                                .equals(methodCallExpr.getNameAsString())) {

                            Optional<Node> parent = methodCallExpr.getParentNode()
                                    .filter(n -> n instanceof ExpressionStmt);
                            if (!parent.isPresent()) {
                                log.warn(methodCallExpr.getParentNode());
                                return;
                            }

                            if (hasSetup.isFalse()) {
                                hasSetup.setTrue();
                                updates.add(LexicalPreservingPrinter.setup(cu));
                            }

                            ExpressionStmt exprStmt = (ExpressionStmt) parent.get();
                            MethodCallExpr srcExpr = exprStmt.getExpression().asMethodCallExpr();

                            ResolvedType srcType = srcExpr.getArgument(0).calculateResolvedType();
                            srcType = getBoundIfInLambda(srcType);
                            ResolvedType destType = srcExpr.getArgument(1).calculateResolvedType();
                            destType = getBoundIfInLambda(destType);

                            // a shallow clone. e.g.: BeanCopyUtil.copy(pageParam, pageParam);
                            if (srcType.describe().equals(destType.describe())) {
                                return;
                            }

                            ClassOrInterfaceDeclaration srcCoid = StaticAstContainer
                                    .getClassOrInterfaceDeclaration(srcType.describe());
                            if (srcCoid == null) {
                                return;
                            }
                            ClassOrInterfaceDeclaration destCoid = StaticAstContainer
                                    .getClassOrInterfaceDeclaration(destType.describe());
                            if (destCoid == null) {
                                return;
                            }
                            String converterName =
                                    srcCoid.getNameAsString() + "To" + destCoid.getNameAsString() + "Converter";

                            // create converter
                            Map<String, Type> srcVars = Maps.newHashMap();
                            srcCoid.getFields().stream().map(field -> field.getVariable(0))
                                    .forEach(var -> srcVars.put(var.getNameAsString(), var.getType()));
                            Map<String, Type> destVars = Maps.newHashMap();
                            destCoid.getFields().stream().map(field -> field.getVariable(0))
                                    .forEach(var -> destVars.put(var.getNameAsString(), var.getType()));

                            Path sourceRoot = cu.getStorage().get().getSourceRoot();
                            String pkg = cu.getPackageDeclaration().get().getNameAsString();
                            String[] pkgParts = pkg.split("\\.");
                            pkgParts[pkgParts.length - 1] = "converter";
                            pkg = Joiner.on('.').join(pkgParts);
                            Collection<ImportDeclaration> imports = Lists.newLinkedList();
                            srcCoid.getFullyQualifiedName()
                                    .ifPresent(ipt -> imports.add(new ImportDeclaration(ipt, false, false)));
                            destCoid.getFullyQualifiedName()
                                    .ifPresent(ipt -> imports.add(new ImportDeclaration(ipt, false, false)));
                            srcCoid.findCompilationUnit().ifPresent(srcCu -> imports.addAll(srcCu.getImports()));
                            destCoid.findCompilationUnit().ifPresent(srcCu -> imports.addAll(srcCu.getImports()));
                            TypeCreator typeCreator = () -> {
                                ClassOrInterfaceDeclaration coid = StaticJavaParser
                                        .parseTypeDeclaration(f("public class %s {}", converterName))
                                        .asClassOrInterfaceDeclaration();
                                MethodDeclaration method = StaticJavaParser.parseMethodDeclaration(
                                        f("public void convert(%s src, %s dest) {}", srcCoid.getName(),
                                                destCoid.getName()));
                                BlockStmt body = method.createBody();
                                srcVars.forEach((name, srcFieldType) -> {
                                    Type destFieldType = destVars.get(name);
                                    ResolvedType resolvedType = srcFieldType.resolve();
                                    if (destFieldType != null && resolvedType.describe()
                                            .equals(destFieldType.resolve().describe())) {
                                        if ("serialVersionUID".equals(name)) {
                                            return;
                                        }
                                        String upper = StringUtils.upperFirstLetter(name);
                                        body.addStatement(StaticJavaParser
                                                .parseExpression(f("dest.set%s(src.get%s())", upper, upper)));
                                    }
                                });
                                coid.addMember(method);
                                return coid;
                            };

                            CuCreator cuCreator = new CuCreator(sourceRoot, pkg, imports, typeCreator);
                            try {
                                creations.add(cuCreator.create(false));
                            } catch (Exception e) {
                                log.warn("mc={}, msg={}", srcExpr, e.getMessage());
                                return;
                            }

                            // replace expression
                            MethodCallExpr replaceExpr = new MethodCallExpr();
                            replaceExpr.setScope(StaticJavaParser.parseExpression("new " + converterName + "()"));
                            replaceExpr.setName("convert");
                            replaceExpr.setArguments(srcExpr.getArguments());
                            exprStmt.setExpression(replaceExpr);
                            cu.addImport(cuCreator.getPrimaryTypeQualifier());
                        }
                    })));
        });

        creations.forEach(Saves::prettySave);
        updates.forEach(Saves::originalSave);

    }

    private static ResolvedType getBoundIfInLambda(ResolvedType type) {
        if (type.isConstraint()) {
            return type.asConstraintType().getBound();
        }
        return type;
    }

}

