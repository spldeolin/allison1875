package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-23
 */
public class OnlyPostMappingStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newArrayList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {
            final Optional<AnnotationExpr> annotateByWhich = isAnnotateByWhich(method, "RequestMapping", "GetMapping",
                    "DeleteMapping", "PutMapping", "PatchMapping");
            if (annotateByWhich.isPresent()) {
                result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                        .setMessage("当前声明的" + annotateByWhich.get() + "是被禁止的"));

                // 替换成PostMapping，方便后续statute扫描handler
                annotateByWhich.get().setName("PostMapping");
            }

        }));

        cus.forEach(cu -> cu.findAll(NormalAnnotationExpr.class).forEach(nae -> {
            if ("RequestMapping".equals(nae.getNameAsString())) {
                nae.getPairs().forEach(pair -> {
                    if ("method".equals(pair.getNameAsString())) {
                        result.add(new LawlessDto(nae).setMessage("只能使用POST请求，禁止再次指定RequestMapping.method"));
                    }
                });
            }
        }));

        return result;
    }

    private Optional<AnnotationExpr> isAnnotateByWhich(MethodDeclaration method, String... anyAnnotation) {
        for (String annotation : anyAnnotation) {
            Optional<AnnotationExpr> annotationByName = method.getAnnotationByName(annotation);
            if (annotationByName.isPresent()) {
                return annotationByName;
            }
        }
        return Optional.empty();
    }

}
