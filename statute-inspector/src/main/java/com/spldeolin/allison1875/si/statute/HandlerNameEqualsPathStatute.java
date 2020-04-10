package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-24
 */
public class HandlerNameEqualsPathStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(coid -> coid.getAnnotationByName("RequestMapping").ifPresent(rm -> {
                        rm.ifSingleMemberAnnotationExpr(restController -> {
                            Expression uri = restController.getMemberValue();
                            this.checkForContoller(result, coid, uri);
                        });
                        rm.ifNormalAnnotationExpr(restController -> restController.getPairs().stream()
                                .filter(pair -> org.apache.commons.lang3.StringUtils
                                        .equalsAny(pair.getNameAsString(), "value", "path"))
                                .findFirst().ifPresent(pair -> {
                                    Expression uri = pair.getValue();
                                    this.checkForContoller(result, coid, uri);
                                }));
                    }));

            cu.findAll(MethodDeclaration.class)
                    .forEach(method -> method.getAnnotationByName("PostMapping").ifPresent(pm -> {
                        pm.ifSingleMemberAnnotationExpr(postMapping -> {
                            Expression uri = postMapping.getMemberValue();
                            this.checkForHandler(result, method, uri);
                        });
                        pm.ifNormalAnnotationExpr(postMapping -> postMapping.getPairs().stream()
                                .filter(pair -> org.apache.commons.lang3.StringUtils
                                        .equalsAny(pair.getNameAsString(), "value", "path"))
                                .findFirst().ifPresent(pair -> {
                                    Expression uri = pair.getValue();
                                    this.checkForHandler(result, method, uri);
                                }));
                    }));
        });
        return result;
    }

    private void checkForHandler(Collection<LawlessDto> result, MethodDeclaration method, Expression uriExpr) {
        String uri = uriExprToUriContent(uriExpr);
        String methodName = method.getNameAsString();
        if (!uri.equals(methodName)) {
            LawlessDto vo = new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                    .setMessage(String.format("URI[%s]与方法名[%s]不一致", uri, methodName));
            result.add(vo);
        }
    }

    private void checkForContoller(Collection<LawlessDto> result, ClassOrInterfaceDeclaration coid,
            Expression uriExpr) {
        String uri = uriExprToUriContent(uriExpr);
        String className = coid.getNameAsString();
        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }
        if (!StringUtils.capture(uri).equals(className)) {
            LawlessDto vo = new LawlessDto(coid,
                    coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new))
                    .setMessage(String.format("URI[%st]与类名[%s]不一致", uri, className));
            result.add(vo);
        }
    }

    private String uriExprToUriContent(Expression uriExpr) {
        if (!uriExpr.isStringLiteralExpr()) {
            return uriExpr.toString();
        }
        String uri = uriExpr.asStringLiteralExpr().getValue();
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        return uri;
    }

}
