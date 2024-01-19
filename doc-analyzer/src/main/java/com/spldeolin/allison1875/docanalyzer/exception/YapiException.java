package com.spldeolin.allison1875.docanalyzer.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2020-08-02
 */
public class YapiException extends Allison1875Exception {

    private static final long serialVersionUID = 379804795379140990L;

    public YapiException(String message) {
        super(message);
    }

    public YapiException(Throwable cause) {
        super(cause);
    }

    public YapiException(String message, Throwable cause) {
        super(message, cause);
    }

}