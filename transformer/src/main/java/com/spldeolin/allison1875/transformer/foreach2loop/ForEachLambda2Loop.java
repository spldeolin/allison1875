package com.spldeolin.allison1875.transformer.foreach2loop;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.TypeParameter;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.util.ast.Saves;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ForEachLambda2Loop {

    public static void main(String[] args) throws FileNotFoundException {

        CompilationUnit demo = StaticJavaParser.parse(new File(
                "/Users/deolin/Documents/project-repo/allison1875/snippet-transformer/src/main/java/com/spldeolin"
                        + "/allison1875/st/foreach2loop/ForEachLambda2Loop.java"));

        for (CompilationUnit cu : AstForest.getInstance()) {
            MutableBoolean update = new MutableBoolean(false);
            for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class,
                    mce -> mce.getNameAsString().equals("forEach") && mce.getArguments().size() == 1)) {
                if (!mce.getScope().isPresent()) {
                    continue;
                }

                String typeName;
                if (mce.getArgument(0).isLambdaExpr()) {
                    LambdaExpr lambda = mce.getArgument(0).asLambdaExpr();
                    try {
                        typeName = Iterables.getLast(Splitter.on('.').splitToList(
                                mce.getScope().get().calculateResolvedType().describe().replace(">", "")
                                        .replace("[]", "")), null);
                    } catch (Exception e) {
                        log.warn(mce.getScope().get());
                        continue;
                    }
                    if (typeName == null) {
                        continue;
                    }

                    if (lambda.getParameters().size() == 1) {
                        BlockStmt loopBody;
                        Statement lambdaBody = lambda.getBody();
                        if (lambdaBody.isExpressionStmt()) {
                            loopBody = new BlockStmt(new NodeList<>(lambdaBody));
                        } else {
                            loopBody = lambdaBody.asBlockStmt();
                        }
                        String name = lambda.getParameter(0).getNameAsString();

                        ForEachStmt forEachStmt = new ForEachStmt(
                                new VariableDeclarationExpr(new TypeParameter(typeName), name), mce.getScope().get(),
                                loopBody);


                        mce.getParentNode().ifPresent(parent -> {
                            if (parent instanceof ExpressionStmt) {
                                ExpressionStmt es = (ExpressionStmt) parent;
                                es.getParentNode().ifPresent(grandParent -> {
                                    if (grandParent instanceof BlockStmt) {
                                        ((BlockStmt) grandParent).getStatements().addAfter(forEachStmt, es);
                                        es.remove();
                                        update.setTrue();
                                    }
                                });
                            }
                        });

                    }
                }

            }
            if (update.isTrue()) {
                Saves.prettySave(cu);
            }
        }
    }

}
