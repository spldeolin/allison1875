package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ListControllersProc {

    Collection<ClassOrInterfaceDeclaration> process(AstForest astForest) {
        Collection<ClassOrInterfaceDeclaration> result = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            result.addAll(cu.findAll(ClassOrInterfaceDeclaration.class, this::isController));
        }
        return result;
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
