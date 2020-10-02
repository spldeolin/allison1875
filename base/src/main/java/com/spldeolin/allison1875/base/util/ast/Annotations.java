package com.spldeolin.allison1875.base.util.ast;

import java.lang.annotation.Annotation;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * @author Deolin 2020-06-10
 */
public class Annotations {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Annotations.class);

    private Annotations() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static <A extends Annotation> boolean isAnnotationPresent(NodeWithAnnotations<?> node,
            Class<A> annotationClass) {
        return getAnnotation(node, annotationClass) != null;
    }

    public static <A extends Annotation> boolean isAnnotationAbsent(NodeWithAnnotations<?> node,
            Class<A> annotationClass) {
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

}
