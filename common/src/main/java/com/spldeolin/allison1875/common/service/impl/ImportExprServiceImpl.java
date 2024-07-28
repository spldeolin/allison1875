package com.spldeolin.allison1875.common.service.impl;

import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-09
 */
@Singleton
@Slf4j
public class ImportExprServiceImpl implements ImportExprService {

    /**
     * 将参数cu中的所有全限定类型抽取成import声明
     *
     * <pre>
     * ClassOrInterfaceType.scope的含义是模棱两可的，
     * JavaParser不知道scope是什么——可能是Map.Entry的Map部分，也可能是java.util.List的java.util部分，
     * 遇到前者的情况时，调用这个方法时，应当排除这部分
     * </pre>
     *
     * @param cu 本方法不会改变这个
     * @see ClassOrInterfaceType
     */
    @Override
    public void extractQualifiedTypeToImport(CompilationUnit cu) {
        // coit
        List<ClassOrInterfaceType> coits = cu.findAll(ClassOrInterfaceType.class, type -> {
            // 防止scope的scope被加入到import，所以只取用name首字母大写的type
            return MoreStringUtils.isFirstLetterUpperCase(type.getNameAsString());
        });
        for (ClassOrInterfaceType coit : coits) {
            coit.getScope().ifPresent(scope -> {
                // 判断scope是否为全小写，这是为了忽略Map.Entry这样的scope情况
                if (!scope.toString().toLowerCase().equals(scope.toString())) {
                    return;
                }
                log.debug("Qualified Type '{}' in '{}' extract to Import", coit,
                        CompilationUnitUtils.getCuAbsolutePath(cu));
                coit.setScope(null);
                cu.addImport(scope + "." + coit.getNameAsString());
            });
        }

        // 2024-07-28：methodDec的Type不同于其他Coit，需要特殊处理
        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
            Type type = md.getType();
            if (type.toString().contains(".")) {
                log.debug("Qualified Type '{}' in '{}' extract to Import", type,
                        CompilationUnitUtils.getCuAbsolutePath(cu));
                cu.addImport(type.toString());
                type.replace(StaticJavaParser.parseType(MoreStringUtils.splitAndGetLastPart(type.toString(), ".")));
            }
        }

        // 注解
        for (AnnotationExpr ae : cu.findAll(AnnotationExpr.class)) {
            if (ae.getNameAsString().contains(".")) {
                cu.addImport(ae.getNameAsString());
                ae.setName(MoreStringUtils.splitAndGetLastPart(ae.getNameAsString(), "."));
            }
        }
    }

    @Override
    public void copyImports(CompilationUnit from, CompilationUnit to) {
        from.getImports().forEach(to::addImport);
    }

}