package com.spldeolin.allison1875.base.collection.ast;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.FieldAbsentException;
import com.spldeolin.allison1875.base.exception.ParentAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import lombok.extern.log4j.Log4j2;

/**
 * FieldDeclaration对象下VariableDeclarator对象的收集器
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
class FieldVariableDeclaratorCollector {

    Collection<VariableDeclarator> collectIntoCollection(Collection<CompilationUnit> cus) {
        Collection<VariableDeclarator> result = Lists.newLinkedList();
        for (CompilationUnit cu : cus) {
            for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                result.addAll(field.getVariables());
            }
        }
        log.info("(Summary) {} VariableDeclarator in field has collected into Collection.", result.size());
        return result;
    }

    Map<String, VariableDeclarator> collectIntoMapByCompilationUnit(Collection<CompilationUnit> cus) {
        Map<String, VariableDeclarator> result = Maps.newHashMap();
        for (CompilationUnit cu : cus) {
            for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                for (VariableDeclarator var : field.getVariables()) {
                    put(field, var, result);
                }
            }
        }
        log.info("(Summary) {} VariableDeclarator in field has collected into Map.", result.size());
        return result;
    }

    Map<String, VariableDeclarator> collectIntoMapByCollectedOnes(Collection<VariableDeclarator> fieldVars) {
        Map<String, VariableDeclarator> result = Maps.newHashMapWithExpectedSize(fieldVars.size());
        for (VariableDeclarator var : fieldVars) {
            FieldDeclaration field = var.findAncestor(FieldDeclaration.class).orElseThrow(FieldAbsentException::new);
            put(field, var, result);
        }
        log.info("(Summary) {} VariableDeclarator in field has collected into Map.", result.size());
        return result;
    }

    private void put(FieldDeclaration field, VariableDeclarator var, Map<String, VariableDeclarator> map) {
        Node parent = field.getParentNode().orElseThrow(ParentAbsentException::new);
        if (!(parent instanceof TypeDeclaration)) {
            // 例如这个field在一个匿名内部类中，那么parent就是ObjectCreationExpr..
            // 由于是在匿名类中，所有没有全限定名，无法通过byQuailfier的方式获取到，自然也不用收集
            return;
        }

        String fieldVarQulifier = Joiner.on(".")
                .join(((TypeDeclaration<?>) parent).getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                        var.getNameAsString());

        if (map.get(fieldVarQulifier) != null) {
            // 多module的maven项目中，这样的不规范情况是可能发生的
            log.warn("Qualifier [{}] is not unique, overwrite collected VariableDeclarator parsed form storage [{}].",
                    fieldVarQulifier,
                    map.get(fieldVarQulifier).findCompilationUnit().orElseThrow(CuAbsentException::new).getStorage()
                            .orElseThrow(StorageAbsentException::new).getPath());
        }
        map.put(fieldVarQulifier, var);
    }

}
