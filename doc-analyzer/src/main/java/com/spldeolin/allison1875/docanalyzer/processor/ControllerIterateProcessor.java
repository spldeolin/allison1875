package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.function.Consumer;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ControllerIterateProcessor {

    private final AstForest astForest;

    public ControllerIterateProcessor(AstForest astForest) {
        this.astForest = astForest;
    }

    public void iterate(Consumer<ClassOrInterfaceDeclaration> eachCoid) {
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
