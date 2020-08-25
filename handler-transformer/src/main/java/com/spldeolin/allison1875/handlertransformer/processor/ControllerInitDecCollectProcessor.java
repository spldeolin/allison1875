package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-27
 */
@Log4j2
class ControllerInitDecCollectProcessor {

    private final CompilationUnit cu;

    @Getter
    private final Collection<Pair<ClassOrInterfaceDeclaration, InitializerDeclaration>> result = Lists.newArrayList();

    public ControllerInitDecCollectProcessor(CompilationUnit cu) {
        this.cu = cu;
    }

    public ControllerInitDecCollectProcessor collect() {
        cu.findAll(ClassOrInterfaceDeclaration.class, this::isController).forEach(controller -> {
            for (BodyDeclaration<?> member : controller.getMembers()) {
                if (!member.isInitializerDeclaration()) {
                    continue;
                }
                InitializerDeclaration init = member.asInitializerDeclaration();
                try {
                    result.add(Pair.of(controller, init));
                } catch (Throwable e) {
                    log.error("InitializerDeclaration fail [{}]", Locations.getRelativePathWithLineNo(init), e);
                }
            }
        });
        return this;
    }

//    public void iterate(TriConsumer<CompilationUnit, ClassOrInterfaceDeclaration, InitializerDeclaration> eachCoid) {
//        astForest
//                .forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class, this::isController).forEach(controller
//                -> {
//                    for (BodyDeclaration<?> member : controller.getMembers()) {
//                        if (!member.isInitializerDeclaration()) {
//                            continue;
//                        }
//                        InitializerDeclaration init = member.asInitializerDeclaration();
//                        result.add(Pair.of(controller, init));
//                    }
//                }));
//    }

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
