package com.spldeolin.allison1875.sqlapigenerator.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2024-01-24
 */
public class AnalyzeSqlException extends Allison1875Exception {

    private static final long serialVersionUID = 5956326864383173625L;

    public AnalyzeSqlException(String message) {
        super(message);
    }

    public AnalyzeSqlException(Throwable cause) {
        super(cause);
    }

    public AnalyzeSqlException(String message, Throwable cause) {
        super(message, cause);
    }

}