package com.spldeolin.allison1875.base.util;

import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.util.Map;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.printer.YamlPrinter;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;

/**
 * @author Deolin 2020-02-15
 */
public class NodeDemo {

    static Map<Class<?>, Integer> nodeCounts = Maps.newHashMap();

    public static void main(String[] args) {
        CONFIG.setDoNotCollectWithLoadingClass(true);
        CompilationUnit max = null;
        int maxCount = 0;
        Map<Class<?>, Integer> countDetail = null;
        for (CompilationUnit cu : StaticAstContainer.getCompilationUnits()) {
            nodeCounts = Maps.newHashMap();
            putCount(cu, ArrayCreationLevel.class);
            putCount(cu, ImportDeclaration.class);
            putCount(cu, Modifier.class);
            putCount(cu, PackageDeclaration.class);
            putCount(cu, AnnotationDeclaration.class);

            putCount(cu, AnnotationMemberDeclaration.class);
            putCount(cu, ClassOrInterfaceDeclaration.class);
            putCount(cu, ConstructorDeclaration.class);
            putCount(cu, EnumConstantDeclaration.class);
            putCount(cu, EnumDeclaration.class);
            putCount(cu, FieldDeclaration.class);
            putCount(cu, InitializerDeclaration.class);
            putCount(cu, MethodDeclaration.class);
            putCount(cu, Parameter.class);
            putCount(cu, ReceiverParameter.class);
            putCount(cu, VariableDeclarator.class);

            putCount(cu, BlockComment.class);
            putCount(cu, JavadocComment.class);
            putCount(cu, LineComment.class);

            putCount(cu, ArrayAccessExpr.class);
            putCount(cu, ArrayCreationExpr.class);
            putCount(cu, ArrayInitializerExpr.class);
            putCount(cu, AssignExpr.class);
            putCount(cu, BinaryExpr.class);
            putCount(cu, BooleanLiteralExpr.class);
            putCount(cu, CastExpr.class);
            putCount(cu, CharLiteralExpr.class);
            putCount(cu, ClassExpr.class);
            putCount(cu, ConditionalExpr.class);
            putCount(cu, DoubleLiteralExpr.class);
            putCount(cu, EnclosedExpr.class);
            putCount(cu, FieldAccessExpr.class);
            putCount(cu, InstanceOfExpr.class);
            putCount(cu, IntegerLiteralExpr.class);
            putCount(cu, LambdaExpr.class);
            putCount(cu, LongLiteralExpr.class);
            putCount(cu, MarkerAnnotationExpr.class);
            putCount(cu, MemberValuePair.class);
            putCount(cu, MethodCallExpr.class);
            putCount(cu, MethodReferenceExpr.class);
            putCount(cu, Name.class);
            putCount(cu, NameExpr.class);
            putCount(cu, NormalAnnotationExpr.class);
            putCount(cu, NullLiteralExpr.class);
            putCount(cu, ObjectCreationExpr.class);
            putCount(cu, SimpleName.class);
            putCount(cu, SingleMemberAnnotationExpr.class);
            putCount(cu, StringLiteralExpr.class);
            putCount(cu, SuperExpr.class);
            putCount(cu, ThisExpr.class);
            putCount(cu, TypeExpr.class);
            putCount(cu, UnaryExpr.class);
            putCount(cu, VariableDeclarationExpr.class);

            putCount(cu, AssertStmt.class);
            putCount(cu, BlockStmt.class);
            putCount(cu, BreakStmt.class);
            putCount(cu, CatchClause.class);
            putCount(cu, ContinueStmt.class);
            putCount(cu, DoStmt.class);
            putCount(cu, EmptyStmt.class);
            putCount(cu, ExplicitConstructorInvocationStmt.class);
            putCount(cu, ExpressionStmt.class);
            putCount(cu, ForEachStmt.class);
            putCount(cu, ForStmt.class);
            putCount(cu, IfStmt.class);
            putCount(cu, LabeledStmt.class);
            putCount(cu, LocalClassDeclarationStmt.class);
            putCount(cu, ReturnStmt.class);
            putCount(cu, SwitchEntry.class);
            putCount(cu, SwitchStmt.class);
            putCount(cu, SynchronizedStmt.class);
            putCount(cu, ThrowStmt.class);
            putCount(cu, TryStmt.class);
            putCount(cu, UnparsableStmt.class);
            putCount(cu, WhileStmt.class);

            putCount(cu, ArrayType.class);
            putCount(cu, ClassOrInterfaceType.class);
            putCount(cu, IntersectionType.class);
            putCount(cu, PrimitiveType.class);
            putCount(cu, TypeParameter.class);
            putCount(cu, UnionType.class);
            putCount(cu, UnknownType.class);
            putCount(cu, VoidType.class);
            putCount(cu, WildcardType.class);

            if (nonZeroNodeCount() > maxCount) {
                max = cu;
                maxCount = nonZeroNodeCount();
                countDetail = nodeCounts;
            }

        }

        System.out.println(max.getStorage().get().getPath());
        System.out.println(maxCount);
        System.out.println(countDetail);

        YamlPrinter.print(max);
    }

    private static void putCount(CompilationUnit cu, Class<? extends Node> clazz) {
        nodeCounts.put(clazz, cu.findAll(clazz).size());
    }

    private static int nonZeroNodeCount() {
        int result = 0;
        for (Integer count : nodeCounts.values()) {
            if (count > 0) {
                result++;
            }
        }
        return result;
    }

}
