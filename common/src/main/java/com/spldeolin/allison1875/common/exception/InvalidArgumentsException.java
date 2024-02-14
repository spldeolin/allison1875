package com.spldeolin.allison1875.common.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2024-02-14
 */
public class InvalidArgumentsException extends Allison1875Exception {

    private static final long serialVersionUID = -3574770931271860394L;

    public InvalidArgumentsException() {
        super((String) null);
    }

    public InvalidArgumentsException(String message) {
        super(message);
    }

    public InvalidArgumentsException(Throwable cause) {
        super(cause);
    }

    public InvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

}