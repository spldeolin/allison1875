package com.spldeolin.allison1875.startransformer.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2023-05-12
 */
public class IllegalChainException extends Allison1875Exception {

    private static final long serialVersionUID = 2656906791188528864L;

    public IllegalChainException(String message) {
        super(message);
    }

    public IllegalChainException(Throwable cause) {
        super(cause);
    }

    public IllegalChainException(String message, Throwable cause) {
        super(message, cause);
    }

}