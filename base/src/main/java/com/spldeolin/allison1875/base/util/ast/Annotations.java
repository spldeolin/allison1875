package com.spldeolin.allison1875.base.util.ast;

import java.lang.annotation.Annotation;
import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-10
 */
@Log4j2
public class Annotations {

    private Annotations() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static <A extends Annotation> boolean isAnnoPresent(NodeWithAnnotations<?> node, Class<A> annotationClass) {
        return findAnno(node, annotationClass) != null;
    }

    public static <A extends Annotation> boolean isAnnoAbsent(NodeWithAnnotations<?> node, Class<A> annotationClass) {
        return !isAnnoPresent(node, annotationClass);
    }

    public static <A extends Annotation> AnnotationExpr findAnno(NodeWithAnnotations<?> node,
            Class<A> annotationClass) {
        Optional<AnnotationExpr> annotation = node.getAnnotationByName(annotationClass.getSimpleName());
        if (annotation.isPresent()) {
            try {
                if (annotationClass.getName().equals(annotation.get().resolve().getQualifiedName())) {
                    return annotation.get();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    public static boolean isAnnoPresent(NodeWithAnnotations<?> node, String annotationQualifier) {
        return findAnno(node, annotationQualifier) != null;
    }

    public static boolean isAnnoAbsent(NodeWithAnnotations<?> node, String annotationQualifier) {
        return !isAnnoPresent(node, annotationQualifier);
    }

    public static AnnotationExpr findAnno(NodeWithAnnotations<?> node, String annotationQualifier) {
        String simpleName = annotationQualifier.substring(annotationQualifier.lastIndexOf('.') + 1);
        Optional<AnnotationExpr> annotation = node.getAnnotationByName(simpleName);
        if (annotation.isPresent()) {
            try {
                if (annotationQualifier.equals(annotation.get().resolve().getQualifiedName())) {
                    return annotation.get();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

}
