package com.spldeolin.allison1875.common.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * 对{@link StaticJavaParser}常用解析方法的增强封装，解析失败时打印原始代码以方便排查
 *
 * @author Deolin 2026-03-15
 */
@Slf4j
public class StaticJavaParserUtils {

    private StaticJavaParserUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 解析Java Statement代码
     *
     * @param code Java代码
     * @return 解析后的Statement AST节点
     */
    public static Statement parseStatement(String code) {
        try {
            return StaticJavaParser.parseStatement(code);
        } catch (Exception e) {
            log.error("Failed to parse statement, code={}", code);
            throw new Allison1875Exception("Failed to parse statement, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Statement代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的Statement AST节点
     */
    public static Statement parseStatement(String template, Object... args) {
        return parseStatement(String.format(template, args));
    }

    /**
     * 解析Java BodyDeclaration代码
     *
     * @param code Java代码
     * @return 解析后的BodyDeclaration AST节点
     */
    public static BodyDeclaration<?> parseBodyDeclaration(String code) {
        try {
            return StaticJavaParser.parseBodyDeclaration(code);
        } catch (Exception e) {
            log.error("Failed to parse body declaration, code={}", code);
            throw new Allison1875Exception("Failed to parse body declaration, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java BodyDeclaration代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的BodyDeclaration AST节点
     */
    public static BodyDeclaration<?> parseBodyDeclaration(String template, Object... args) {
        return parseBodyDeclaration(String.format(template, args));
    }

    /**
     * 解析Java FieldDeclaration代码
     *
     * @param code Java代码
     * @return 解析后的FieldDeclaration AST节点
     */
    public static FieldDeclaration parseFieldDeclaration(String code) {
        try {
            return StaticJavaParser.parseBodyDeclaration(code).asFieldDeclaration();
        } catch (Exception e) {
            log.error("Failed to parse field declaration, code={}", code);
            throw new Allison1875Exception("Failed to parse field declaration, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java FieldDeclaration代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的FieldDeclaration AST节点
     */
    public static FieldDeclaration parseFieldDeclaration(String template, Object... args) {
        return parseFieldDeclaration(String.format(template, args));
    }

    /**
     * 解析Java MethodDeclaration代码
     *
     * @param code Java代码
     * @return 解析后的MethodDeclaration AST节点
     */
    public static MethodDeclaration parseMethodDeclaration(String code) {
        try {
            return StaticJavaParser.parseBodyDeclaration(code).asMethodDeclaration();
        } catch (Exception e) {
            log.error("Failed to parse method declaration, code={}", code);
            throw new Allison1875Exception("Failed to parse method declaration, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java MethodDeclaration代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的MethodDeclaration AST节点
     */
    public static MethodDeclaration parseMethodDeclaration(String template, Object... args) {
        return parseMethodDeclaration(String.format(template, args));
    }

    /**
     * 解析Java Type代码
     *
     * @param code Java类型代码
     * @return 解析后的Type AST节点
     */
    public static Type parseType(String code) {
        try {
            return StaticJavaParser.parseType(code);
        } catch (Exception e) {
            log.error("Failed to parse type, code={}", code);
            throw new Allison1875Exception("Failed to parse type, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Type代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的Type AST节点
     */
    public static Type parseType(String template, Object... args) {
        return parseType(String.format(template, args));
    }

    /**
     * 解析Java Annotation代码
     *
     * @param code Java注解代码
     * @return 解析后的AnnotationExpr AST节点
     */
    public static AnnotationExpr parseAnnotation(String code) {
        try {
            return StaticJavaParser.parseAnnotation(code);
        } catch (Exception e) {
            log.error("Failed to parse annotation, code={}", code);
            throw new Allison1875Exception("Failed to parse annotation, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Annotation代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的AnnotationExpr AST节点
     */
    public static AnnotationExpr parseAnnotation(String template, Object... args) {
        return parseAnnotation(String.format(template, args));
    }

    /**
     * 解析Java VariableDeclarationExpr代码
     *
     * @param code Java变量声明代码
     * @return 解析后的VariableDeclarationExpr AST节点
     */
    public static VariableDeclarationExpr parseVariableDeclarationExpr(String code) {
        try {
            return StaticJavaParser.parseVariableDeclarationExpr(code);
        } catch (Exception e) {
            log.error("Failed to parse variable declaration expression, code={}", code);
            throw new Allison1875Exception("Failed to parse variable declaration expression, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java VariableDeclarationExpr代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的VariableDeclarationExpr AST节点
     */
    public static VariableDeclarationExpr parseVariableDeclarationExpr(String template, Object... args) {
        return parseVariableDeclarationExpr(String.format(template, args));
    }

    /**
     * 解析Java Expression代码
     *
     * @param code Java表达式代码
     * @return 解析后的Expression AST节点
     */
    public static <T extends Expression> T parseExpression(String code) {
        try {
            return StaticJavaParser.parseExpression(code);
        } catch (Exception e) {
            log.error("Failed to parse expression, code={}", code);
            throw new Allison1875Exception("Failed to parse expression, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Expression代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的Expression AST节点
     */
    public static <T extends Expression> T parseExpression(String template, Object... args) {
        return parseExpression(String.format(template, args));
    }

    /**
     * 解析Java Block代码
     *
     * @param code Java代码块
     * @return 解析后的BlockStmt AST节点
     */
    public static BlockStmt parseBlock(String code) {
        try {
            return StaticJavaParser.parseBlock(code);
        } catch (Exception e) {
            log.error("Failed to parse block, code={}", code);
            throw new Allison1875Exception("Failed to parse block, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Block代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的BlockStmt AST节点
     */
    public static BlockStmt parseBlock(String template, Object... args) {
        return parseBlock(String.format(template, args));
    }

    /**
     * 解析Java Parameter代码
     *
     * @param code Java参数代码
     * @return 解析后的Parameter AST节点
     */
    public static Parameter parseParameter(String code) {
        try {
            return StaticJavaParser.parseParameter(code);
        } catch (Exception e) {
            log.error("Failed to parse parameter, code={}", code);
            throw new Allison1875Exception("Failed to parse parameter, code=" + code, e);
        }
    }

    /**
     * 使用{@link String#format(String, Object...)}格式化后解析Java Parameter代码
     *
     * @param template 代码模板
     * @param args 格式化参数
     * @return 解析后的Parameter AST节点
     */
    public static Parameter parseParameter(String template, Object... args) {
        return parseParameter(String.format(template, args));
    }

}
