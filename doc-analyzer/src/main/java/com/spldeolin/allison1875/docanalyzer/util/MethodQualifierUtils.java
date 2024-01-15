package com.spldeolin.allison1875.docanalyzer.util;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

/**
 * @author Deolin 2019-12-29
 */
public class MethodQualifierUtils {

    private MethodQualifierUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 获取能定位到唯一一个方法的最短形式QualifiedSignature（内部类的$替换成.）
     * e.g.: com.spldeolin.allison1875.da.core.util.MethodQualifiers.getShortestQualifiedSignature(com.github
     * .javaparser.ast.body.MethodDeclaration)
     */
    public static String getShortestQualifiedSignature(Method method) {
        StringBuilder result = new StringBuilder(64);

        // class qualifier and method name
        String typeName = method.getDeclaringClass().getTypeName();
        typeName = dollarToDot(typeName);
        result.append(typeName).append('.');
        result.append(method.getName());

        // every parameter qualifier
        result.append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int j = 0; j < parameterTypes.length; j++) {
            result.append(parameterTypes[j].getTypeName());
            if (j < (parameterTypes.length - 1)) {
                result.append(",");
            }
        }
        result.append(')');

        return trimAllSpaces(result).replaceAll("\\$", ".");
    }

    /**
     * 获取能定位到唯一一个方法的最短形式QualifiedSignature
     * e.g.: com.spldeolin.allison1875.da.core.util.MethodQualifiers.getShortestQualifiedSignature(com.github
     * .javaparser.ast.body.MethodDeclaration)
     */
    public static String getShortestQualifiedSignature(MethodDeclaration methodDeclaration) {
        ResolvedMethodDeclaration resolve = methodDeclaration.resolve();

        String result = resolve.getQualifiedSignature();
        // remove every <*>
        result = result.replaceAll("<[^>]+>", "");
        return trimAllSpaces(dollarToDot(result));
    }

    /**
     * e.g.: com.spldeolin.allison1875.da.core.util.MethodQualifiers.getTypeQualifierWithMethodName
     */
    public static String getTypeQualifierWithMethodName(MethodDeclaration methodDeclaration) {
        StringBuilder sb = new StringBuilder(64);
        methodDeclaration.findAncestor(TypeDeclaration.class).map(tp -> (TypeDeclaration<?>) tp)
                .ifPresent(tp -> tp.getFullyQualifiedName().ifPresent(sb::append));
        sb.append(".");
        sb.append(methodDeclaration.getNameAsString());
        return sb.toString();
    }

    private static String dollarToDot(String s) {
        return s.replaceAll("\\$", ".");
    }

    private static String trimAllSpaces(CharSequence s) {
        return s.toString().replaceAll(" ", "");
    }

}
