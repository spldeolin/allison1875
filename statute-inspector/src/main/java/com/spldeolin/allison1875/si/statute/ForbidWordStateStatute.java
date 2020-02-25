package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-25
 */
public class ForbidWordStateStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(NameExpr.class).forEach(ne -> {
            if (ne.toString().contains("state")) {
                result.add(new LawlessDto(ne));
            }
        }));
        return result;
    }

}
