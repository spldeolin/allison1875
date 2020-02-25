package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-16
 */
@Log4j2
public class MultiNewResponseInfoStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {
            int count = method.findAll(ObjectCreationExpr.class, oce -> oce.getTypeAsString().equals("ResponseInfo"))
                    .size();
            if (count > 1) {
                LawlessDto vo = new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                        .setMessage("new ResponseInfo出现了" + count + "次");
                result.add(vo);
            }
        }));
        return result;
    }

}
