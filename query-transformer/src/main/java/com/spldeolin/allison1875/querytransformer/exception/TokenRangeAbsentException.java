package com.spldeolin.allison1875.querytransformer.exception;

import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2024-02-11
 */
public class TokenRangeAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = -3600283594310990482L;

    public TokenRangeAbsentException(Node node) {
        super("Node [" + node.toString() + "] has no Token Range");
    }

    public TokenRangeAbsentException(String message) {
        super(message);
    }

    public TokenRangeAbsentException(Throwable cause) {
        super(cause);
    }

    public TokenRangeAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}