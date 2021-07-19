package com.spldeolin.allison1875.base.util.ast;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-07-19
 */
@Log4j2
public class Javabeans {

    public static void removeUnusageImports(CompilationUnit javabeanCu) {
        // 获取所有lombok.开头以外的import声明
        List<ImportDeclaration> imports = Lists.newArrayList(javabeanCu.getImports());
        imports.removeIf(i -> i.getNameAsString().startsWith("lombok."));
        if (imports.size() == 0) {
            return;
        }

        // resolve所有Type与Expression，收集describes
        Set<String> describes = Sets.newHashSet();
        for (Type type : javabeanCu.findAll(Type.class)) {
            if (type.isReferenceType()) {
                try {
                    String describe = type.resolve().describe();
                    describes.add(describe);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        }
        for (Expression expression : javabeanCu.findAll(Expression.class)) {
            try {
                String describe = expression.calculateResolvedType().describe();
                describes.add(describe);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        // 删除未被使用的 非.*结尾的import声明
        Set<ImportDeclaration> unAsterisks = imports.stream().filter(i -> !i.isAsterisk()).collect(Collectors.toSet());
        for (ImportDeclaration unAsterisk : unAsterisks) {
            if (!describes.contains(unAsterisk.getNameAsString())) {
                unAsterisk.remove();
            }
        }

        // 删除未被使用的 .*结尾的import声明
        Set<ImportDeclaration> asterisks = imports.stream().filter(ImportDeclaration::isAsterisk)
                .collect(Collectors.toSet());
        if (asterisks.size() > 0) {
            Set<String> substringDescribe = describes.stream().map(d -> StringUtils.substringBeforeLast(d, "."))
                    .collect(Collectors.toSet());
            for (ImportDeclaration asterisk : asterisks) {
                if (!substringDescribe.contains(asterisk.getNameAsString())) {
                    asterisk.remove();
                }
            }
        }
    }

}