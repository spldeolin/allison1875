package com.spldeolin.allison1875.common.ancestor;

/**
 * @author Deolin 2023-12-28
 */
public abstract class Allison1875BaseException extends RuntimeException {

    private static final long serialVersionUID = -801498553581107750L;

    public Allison1875BaseException(String message) {
        super(message);
    }

    public Allison1875BaseException(Throwable cause) {
        super(cause);
    }

    public Allison1875BaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
