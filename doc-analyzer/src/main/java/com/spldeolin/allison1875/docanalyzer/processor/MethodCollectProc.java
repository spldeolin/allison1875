package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 收集coid下所有method的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class MethodCollectProc {

    public Map<String, MethodDeclaration> collectMethods(ClassOrInterfaceDeclaration coid) {
        Map<String, MethodDeclaration> methods = Maps.newHashMap();
        for (MethodDeclaration method : coid.findAll(MethodDeclaration.class)) {
            try {
                methods.put(MethodQualifiers.getShortestQualifiedSignature(method), method);
            } catch (Exception e) {
                log.warn("fail to get shortest qualified signature [{}]", method.getNameAsString(), e);
            }
        }
        return methods;
    }

}
