package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.MethodQualifiers;
import com.spldeolin.allison1875.si.vo.LawlessVo;

/**
 * @author Deolin 2020-02-23
 */
public class OnlyPostMappingStatute implements Statute {

    @Override
    public Collection<LawlessVo> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessVo> result = Lists.newArrayList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {
            String annotateByWhich = isAnnotateByWhich(method, "RequestMapping", "GetMapping", "DeleteMapping",
                    "PutMapping", "PatchMapping");
            if (annotateByWhich != null) {
                result.add(new LawlessVo(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                        .setMessage("当前声明的" + annotateByWhich + "是被禁止的"));
            }
        }));

        cus.forEach(cu -> cu.findAll(NormalAnnotationExpr.class).forEach(nae -> {
            if ("RequestMapping".equals(nae.getNameAsString())) {
                nae.getPairs().forEach(pair -> {
                    if ("method".equals(pair.getNameAsString())) {
                        result.add(new LawlessVo(nae).setMessage("只能使用POST请求，禁止再次指定RequestMapping.method"));
                    }
                });
            }
        }));

        return result;
    }

    private String isAnnotateByWhich(MethodDeclaration method, String... anyAnnotation) {
        for (String annotation : anyAnnotation) {
            if (method.getAnnotationByName(annotation).isPresent()) {
                return annotation;
            }
        }
        return null;
    }

}
