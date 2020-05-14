package com.spldeolin.allison1875.base.collection.ast;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.log4j.Log4j2;

/**
 * TypeDeclaration对象的收集器
 *
 * @param <T> TypeDeclaration的具体类型
 * @author Deolin 2020-02-03
 */
@Log4j2
class TypeDeclarationByNameCollector<T extends TypeDeclaration<?>> {

    private final Class<T> type;

    TypeDeclarationByNameCollector(Class<T> type) {
        this.type = type;
    }

    Multimap<String, T> collectIntoMapByCompilationUnits(Collection<CompilationUnit> cus) {
        Multimap<String, T> result = ArrayListMultimap.create();
        for (CompilationUnit cu : cus) {
            putAll(cu.findAll(type), result);
        }
        log.info("(Summary) {} {} has collected into Map.", result.size(), type.getSimpleName());
        return result;
    }

    Multimap<String, T> collectIntoMapByCollectedOnes(Collection<T> typeDeclarations) {
        Multimap<String, T> result = ArrayListMultimap.create(typeDeclarations.size(), 1);
        putAll(typeDeclarations, result);
        log.info("(Summary) {} {} has collected into Map.", result.size(), type.getSimpleName());
        return result;
    }

    private void putAll(Collection<T> coids, Multimap<String, T> map) {
        for (T coid : coids) {
            map.put(coid.getNameAsString(), coid);
        }
    }

}
