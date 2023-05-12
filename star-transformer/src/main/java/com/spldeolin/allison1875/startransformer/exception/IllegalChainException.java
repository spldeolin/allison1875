package com.spldeolin.allison1875.startransformer.exception;

/**
 * @author Deolin 2023-05-12
 */
public class IllegalChainException extends RuntimeException {

    private static final long serialVersionUID = 2656906791188528864L;

    public IllegalChainException() {
    }

    public IllegalChainException(String message) {
        super(message);
    }

    public IllegalChainException(Throwable cause) {
        super(cause);
    }

}