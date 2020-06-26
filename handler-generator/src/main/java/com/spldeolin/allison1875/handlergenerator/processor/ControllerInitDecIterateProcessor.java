package com.spldeolin.allison1875.handlergenerator.processor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlergenerator.util.TriConsumer;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-27
 */
@Log4j2
class ControllerInitDecIterateProcessor {

    private final AstForest astForest;

    public ControllerInitDecIterateProcessor(AstForest astForest) {
        this.astForest = astForest;
    }

    public void iterate(TriConsumer<CompilationUnit, ClassOrInterfaceDeclaration, InitializerDeclaration> eachCoid) {
        astForest
                .forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class, this::isController).forEach(controller -> {
                    for (BodyDeclaration<?> member : controller.getMembers()) {
                        if (!member.isInitializerDeclaration()) {
                            continue;
                        }
                        InitializerDeclaration init = member.asInitializerDeclaration();
                        try {
                            eachCoid.accept(cu, controller, init);
                        } catch (Throwable e) {
                            log.error("InitializerDeclaration fail [{}]", Locations.getRelativePathWithLineNo(init), e);
                        }
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
