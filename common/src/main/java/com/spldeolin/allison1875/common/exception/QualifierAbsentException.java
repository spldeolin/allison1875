package com.spldeolin.allison1875.common.exception;

import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2019-12-13
 */
public class QualifierAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = -6063996782617984285L;

    public QualifierAbsentException(NodeWithSimpleName<?> node) {
        super("Node [" + node.getNameAsString() + "] has no Qualifier");
    }

    public QualifierAbsentException(String message) {
        super(message);
    }

    public QualifierAbsentException(Throwable cause) {
        super(cause);
    }

    public QualifierAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}
