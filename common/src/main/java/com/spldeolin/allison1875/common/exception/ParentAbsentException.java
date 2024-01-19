package com.spldeolin.allison1875.common.exception;

import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2019-12-20
 */
public class ParentAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = 8563197517438278052L;

    public ParentAbsentException(NodeWithSimpleName<?> node) {
        super("Node [" + node.getNameAsString() + "] has no Parent Node");
    }

    public ParentAbsentException(String message) {
        super(message);
    }

    public ParentAbsentException(Throwable cause) {
        super(cause);
    }

    public ParentAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}
