package com.spldeolin.allison1875.querytransformer.exception;

/**
 * @author Deolin 2021-05-29
 */
public class IllegalChainException extends RuntimeException {

    public IllegalChainException() {
    }

    public IllegalChainException(String message) {
        super(message);
    }

    public IllegalChainException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 3800789440778078756L;

}