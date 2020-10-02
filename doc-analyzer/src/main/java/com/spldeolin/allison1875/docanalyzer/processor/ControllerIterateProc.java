package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-10
 */
class ControllerIterateProc {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ControllerIterateProc.class);

    private final AstForest astForest;

    ControllerIterateProc(AstForest astForest) {
        this.astForest = astForest;
    }

    void iterate(Consumer<ClassOrInterfaceDeclaration> eachCoid) {
        astForest
                .forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class, this::isController).forEach(controller -> {
                    try {
                        eachCoid.accept(controller);
                    } catch (Throwable t) {
                        log.error("controller fail [{}]", controller.getFullyQualifiedName(), t);
                    }
                }));
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(QualifierConstants.CONTROLLER) || QualifierConstants.CONTROLLER
                        .equals(resolve.getQualifiedName())) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

}
