package com.spldeolin.allison1875.querytransformer.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2021-05-29
 */
public class IllegalChainException extends Allison1875Exception {

    private static final long serialVersionUID = 3800789440778078756L;

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