package com.spldeolin.allison1875.querytransformer.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2021-05-29
 */
public class IllegalDesignException extends Allison1875Exception {

    private static final long serialVersionUID = 3800789440778078756L;

    public IllegalDesignException(String message) {
        super(message);
    }

    public IllegalDesignException(Throwable cause) {
        super(cause);
    }

    public IllegalDesignException(String message, Throwable cause) {
        super(message, cause);
    }

}