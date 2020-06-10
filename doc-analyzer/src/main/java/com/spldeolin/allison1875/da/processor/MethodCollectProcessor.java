package com.spldeolin.allison1875.da.processor;

import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;

/**
 * @author Deolin 2020-06-10
 */
public class MethodCollectProcessor {

    public Map<String, MethodDeclaration> collectMethods(ClassOrInterfaceDeclaration coid) {
        Map<String, MethodDeclaration> methods = Maps.newHashMap();
        for (MethodDeclaration method : coid.findAll(MethodDeclaration.class)) {
            methods.put(MethodQualifiers.getShortestQualifiedSignature(method), method);
        }
        return methods;
    }

}
