package com.spldeolin.allison1875.si;

import static com.spldeolin.allison1875.base.util.Locations.getBeginLine;
import static com.spldeolin.allison1875.base.util.Locations.getRelativePath;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.base.GlobalCollectionStrategy;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出POJO中缺少注释的field
 *
 * 目前认为有@Data注解的类是POJO
 *
 * @author Deolin 2020-02-09
 */
@Log4j2
public class CommentAbsentFiledReporter {

    public static void main(String[] args) {
        new CommentAbsentFiledReporter().process();
    }

    private void process() {
        GlobalCollectionStrategy.setDoNotCollectWithLoadingClass(false);
        StaticGitAddedFileContainer.removeIfNotContain(StaticAstContainer.getClassOrInterfaceDeclarations())
                .forEach(coid -> {
                    if (!isPojo(coid)) {
                        return;
                    }
                    coid.getFields().forEach(field -> {
                        if (!field.getJavadoc().isPresent()) {
                            log.info("{}:{}", getRelativePath(field), getBeginLine(field));
                        }
                    });
                });
    }

    private boolean isPojo(ClassOrInterfaceDeclaration coid) {
        return coid.getAnnotationByName("Data").isPresent();
    }

}
