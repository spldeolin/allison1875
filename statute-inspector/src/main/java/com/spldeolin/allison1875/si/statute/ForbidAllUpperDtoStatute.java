package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-25
 */
public class ForbidAllUpperDtoStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
            if (coid.getNameAsString().endsWith("DTO")) {
                result.add(
                        new LawlessDto(coid, coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
            }
        }));
        return result;
    }

}
