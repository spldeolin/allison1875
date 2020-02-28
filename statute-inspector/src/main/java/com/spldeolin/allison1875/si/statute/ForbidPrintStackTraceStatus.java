package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-28
 */
public class ForbidPrintStackTraceStatus implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(MethodCallExpr.class).forEach(mce -> {
            mce.getScope().ifPresent(scope -> {

                // calculateResolvedType的性能开销比较大

//                ResolvedType rt;
//                try {
//                    rt = scope.calculateResolvedType();
//                } catch (Exception ignored) {
//                    return;
//                }
//
//                if (!rt.isReferenceType()) {
//                    return;
//                }
//                if (rt.asReferenceType().getAllAncestors().stream()
//                        .anyMatch(ancestor -> QualifierConstants.THROWABLE.equals(ancestor.getId()))) {
                if ("printStackTrace".equals(mce.getNameAsString())) {
                    result.add(new LawlessDto(mce).setMessage("禁止出现" + mce));
                }
//                }
            });
        }));


        return result;
    }

}
