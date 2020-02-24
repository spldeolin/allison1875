package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-09
 */
@Log4j2
public class CommentAbsentFiledStatute implements Statute {

    private boolean isTarget(ClassOrInterfaceDeclaration coid) {
        boolean result = coid.getNameAsString().endsWith("Req");
        result |= coid.getNameAsString().endsWith("Resp");
        return result;
    }

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newArrayList();

        cus.forEach(
                cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(this::isTarget).forEach(coid -> {
                    coid.findAll(FieldDeclaration.class).forEach(field -> {
                        if (!field.getJavadoc().isPresent()) {
                            LawlessDto vo = new LawlessDto(field,
                                    coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new) + "."
                                            + field.getVariable(0).getNameAsString());
                            result.add(vo);
                        }
                    });
                }));

        return result;
    }

}
