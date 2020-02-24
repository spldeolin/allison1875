package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-13
 */
@Log4j2
public class NormalControllerStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();

        cus.forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(coid -> coid.getAnnotationByName("Controller").ifPresent(anno -> {
                    LawlessDto vo = new LawlessDto(coid,
                            coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
                    result.add(vo.setMessage("控制器只能使用@RestController声明"));
                })));

        StaticGitAddedFileContainer.removeIfNotContain(cus)
                .forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
                    Optional<AnnotationExpr> restController = coid.getAnnotationByName("RestController");
                    if (!restController.isPresent()) {
                        return;
                    }
                    Optional<AnnotationExpr> requestMapping = coid.getAnnotationByName("RequestMapping");
                    if (!requestMapping.isPresent()) {
                        LawlessDto vo = new LawlessDto(coid,
                                coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
                        result.add(vo.setMessage("控制器必须有@RequestMapping"));
                    }
                }));

        return result;
    }

}
