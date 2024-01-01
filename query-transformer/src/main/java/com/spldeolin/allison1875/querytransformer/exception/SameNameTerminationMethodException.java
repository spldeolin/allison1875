package com.spldeolin.allison1875.querytransformer.exception;

/**
 * @author Deolin 2022-01-16
 */
public class SameNameTerminationMethodException extends RuntimeException {

    private static final long serialVersionUID = 4059564619781745780L;

    public SameNameTerminationMethodException() {
    }

    public SameNameTerminationMethodException(String message) {
        super(message);
    }

    public SameNameTerminationMethodException(Throwable cause) {
        super(cause);
    }

}