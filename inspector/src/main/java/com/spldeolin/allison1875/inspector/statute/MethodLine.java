package com.spldeolin.allison1875.inspector.statute;

import java.util.Collection;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;

/**
 * @author Deolin 2020-08-31
 */
public class MethodLine implements Statute {

    @Override
    public Collection<LawlessDto> inspect(CompilationUnit cu) {
        Collection<LawlessDto> result = Lists.newArrayList();
        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            Range range = method.getRange().orElseThrow(RangeAbsentException::new);
            int i = range.end.line - range.begin.line + 1;
            if (i > 200) {
                result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method),
                        String.format("方法不能超过200行，当前%s行", i)));
            }
        }
        return result;
    }

}