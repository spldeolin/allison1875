package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-25
 */
public class ForbidBroadWordStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(this::isAnyReqRespAoVo)
                .forEach(coid -> coid.getFields().forEach(field -> field.getVariables().forEach(var -> {
                    if (StringUtils.equalsAny(var.getNameAsString(), "id", "name", "status", "type")) {
                        LawlessDto vo = new LawlessDto(field,
                                coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new) + "." + field
                                        .getVariable(0).getNameAsString());
                        result.add(vo);
                    }
                }))));
        return result;
    }

    private boolean isAnyReqRespAoVo(ClassOrInterfaceDeclaration coid) {
        String name = coid.getNameAsString();
        return name.endsWith("Req") || name.endsWith("Resp") || name.endsWith("Ao") || name.endsWith("Vo");
    }

}
