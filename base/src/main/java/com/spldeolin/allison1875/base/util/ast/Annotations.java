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

    public static <A extends Annotation> boolean isAnnotationPresent(NodeWithAnnotations<?> node, Class<A> annotationClass) {
        return getAnnotation(node, annotationClass) != null;
    }

    public static <A extends Annotation> boolean isAnnotationAbsent(NodeWithAnnotations<?> node, Class<A> annotationClass) {
        return !isAnnotationPresent(node, annotationClass);
    }

    public static <A extends Annotation> AnnotationExpr getAnnotation(NodeWithAnnotations<?> node,
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

    public static boolean isAnnotationPresent(NodeWithAnnotations<?> node, String annotationQualifier) {
        return getAnnotation(node, annotationQualifier) != null;
    }

    public static boolean isAnnotationAbsent(NodeWithAnnotations<?> node, String annotationQualifier) {
        return !isAnnotationPresent(node, annotationQualifier);
    }

    public static AnnotationExpr getAnnotation(NodeWithAnnotations<?> node, String annotationQualifier) {
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
