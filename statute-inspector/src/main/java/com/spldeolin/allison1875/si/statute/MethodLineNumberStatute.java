package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-10
 */
@Log4j2
public class MethodLineNumberStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(
                cu -> cu.findAll(MethodDeclaration.class, method -> method.getBody().isPresent()).forEach(method -> {
                    Range range = Locations.getRange(method);
                    int lineCount = range.end.line - range.begin.line + 1;
                    if (lineCount > 200) {
                        LawlessDto vo = new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method));
                        result.add(vo.setMessage("行数已超过200，当前：" + lineCount));
                    }
                }));
        return result;
    }

}
