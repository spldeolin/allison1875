package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.constant.ControllerMarkerConstant;
import com.spldeolin.allison1875.docanalyzer.javabean.ControllerFullDto;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ListControllersProc {

    Collection<ControllerFullDto> process(AstForest astForest) {
        Collection<ControllerFullDto> result = Lists.newArrayList();
        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : cu
                    .findAll(ClassOrInterfaceDeclaration.class, this::isController)) {
                if (findIgnoreFlag(controller)) {
                    continue;
                }

                // 反射controller，如果失败那么这个controller就没有处理该controller的必要了
                Class<?> reflectionController;
                try {
                    reflectionController = tryReflectController(controller);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                String controllerCat = findControllerCat(controller);

                result.add(new ControllerFullDto(controllerCat, controller, reflectionController));
            }
        }
        return result;
    }

    private String findControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = findCat(controller);
        if (controllerCat == null) {
            controllerCat = JavadocDescriptions.getFirstLine(controller);
        }
        if (StringUtils.isEmpty(controllerCat)) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

    private String findCat(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_CAT)) {
                String catContent = org.apache.commons.lang3.StringUtils
                        .removeStartIgnoreCase(line, ControllerMarkerConstant.DOC_CAT).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_IGNORE)) {
                return true;
            }
        }
        return false;
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

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller) throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        try {
            return LoadClassUtils.loadClass(qualifier, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("类[{}]无法被加载", qualifier);
            throw e;
        }
    }

}
