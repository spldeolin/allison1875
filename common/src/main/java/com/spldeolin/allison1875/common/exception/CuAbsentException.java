package com.spldeolin.allison1875.common.exception;

import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2020-02-03
 */
public class CuAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = 2660518405933317294L;

    public CuAbsentException(NodeWithSimpleName<?> node) {
        super("Node [" + node.getNameAsString() + "] is not in Compilation Unit");
    }

//    public CuAbsentException(String message) {
//        super(message);
//    }

    public CuAbsentException(Throwable cause) {
        super(cause);
    }

    public CuAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}
