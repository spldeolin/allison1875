package com.spldeolin.allison1875.si.statute;

import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;
import static com.spldeolin.allison1875.base.util.Locations.getBeginLine;
import static com.spldeolin.allison1875.base.util.Locations.getRelativePath;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.si.vo.LawlessVo;
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
    public Collection<LawlessVo> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessVo> result = Lists.newArrayList();

        StaticGitAddedFileContainer.removeIfNotContain(cus).forEach(
                cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(this::isTarget).forEach(coid -> {
                    coid.findAll(FieldDeclaration.class).forEach(field -> {
                        if (!field.getJavadoc().isPresent()) {
                            LawlessVo vo = new LawlessVo(field,
                                    coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new) + "."
                                            + field.getVariable(0).getNameAsString());
                            result.add(vo);
                        }
                    });
                }));

        return result;
    }

}
