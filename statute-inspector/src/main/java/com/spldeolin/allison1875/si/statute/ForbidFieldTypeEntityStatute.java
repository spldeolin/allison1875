package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-27
 */
public class ForbidFieldTypeEntityStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(FieldDeclaration.class).forEach(field -> {
            field.getVariables().forEach(var -> {
                if (var.getTypeAsString().endsWith("Entity")) {
                    result.add(new LawlessDto(field));
                }
            });
        }));
        return result;
    }

}
