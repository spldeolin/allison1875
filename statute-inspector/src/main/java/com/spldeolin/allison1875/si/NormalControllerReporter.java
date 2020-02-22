package com.spldeolin.allison1875.si;

import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出声明@Controller的普通控制器
 *
 * @author Deolin 2020-02-13
 */
@Log4j2
public class NormalControllerReporter {

    private void process() {
        StaticGitAddedFileContainer.removeIfNotContain(StaticAstContainer.getClassOrInterfaceDeclarations())
                .forEach(coid -> coid.getAnnotationByName("Controller").ifPresent(anno -> {
                    ResolvedAnnotationDeclaration resolve = anno.resolve();
                    if (QualifierConstants.CONTROLLER.equals(resolve.getId())) {
                        log.info(coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
                    }
                }));
    }

    public static void main(String[] args) {
        new NormalControllerReporter().process();
    }

}
