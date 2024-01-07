package com.spldeolin.allison1875.common.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2023-12-28
 */
public class CompilationUnitParseException extends Allison1875Exception {

    private static final long serialVersionUID = -481993432355887421L;

    public CompilationUnitParseException(String message) {
        super(message);
    }

    public CompilationUnitParseException(Throwable cause) {
        super(cause);
    }

    public CompilationUnitParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
