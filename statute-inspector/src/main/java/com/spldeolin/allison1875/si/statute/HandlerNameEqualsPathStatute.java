package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.Strings;
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
                        String className = coid.getNameAsString();
                        rm.ifSingleMemberAnnotationExpr(restController -> {
                            String uri = restController.getMemberValue().toString();
                            this.checkForContoller(result, coid, className, uri);
                        });
                        rm.ifNormalAnnotationExpr(restController -> restController.getPairs().stream()
                                .filter(pair -> StringUtils.equalsAny(pair.getNameAsString(), "value", "path"))
                                .findFirst().ifPresent(pair -> {
                                    String uri = pair.getValue().toString();
                                    this.checkForContoller(result, coid, className, uri);
                                }));
                    }));

            cu.findAll(MethodDeclaration.class)
                    .forEach(method -> method.getAnnotationByName("PostMapping").ifPresent(pm -> {
                        String methodName = method.getNameAsString();
                        pm.ifSingleMemberAnnotationExpr(postMapping -> {
                            String uri = postMapping.getMemberValue().toString();
                            this.checkForHandler(result, method, methodName, uri);
                        });
                        pm.ifNormalAnnotationExpr(postMapping -> postMapping.getPairs().stream()
                                .filter(pair -> StringUtils.equalsAny(pair.getNameAsString(), "value", "path"))
                                .findFirst().ifPresent(pair -> {
                                    String uri = pair.getValue().toString();
                                    this.checkForHandler(result, method, methodName, uri);
                                }));
                    }));
        });
        return result;
    }

    private void checkForHandler(Collection<LawlessDto> result, MethodDeclaration method, String methodName,
            String uri) {
        if (!uri.equals(methodName)) {
            LawlessDto vo = new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                    .setMessage(String.format("URI[%s]与方法名[%s]不一致", uri, methodName));
            result.add(vo);
        }
    }

    private void checkForContoller(Collection<LawlessDto> result, ClassOrInterfaceDeclaration coid, String className,
            String uri) {
        if (!Strings.capture(uri).equals(className)) {
            LawlessDto vo = new LawlessDto(coid,
                    coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new))
                    .setMessage(String.format("URI[%s]与类名[%s]不一致", uri, className));
            result.add(vo);
        }
    }

}
