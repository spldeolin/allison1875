package com.spldeolin.allison1875.docanalyzer.exception;

/**
 * @author Deolin 2020-08-02
 */
public class YapiException extends RuntimeException {

    public YapiException() {
        super();
    }

    public YapiException(String message) {
        super(message);
    }

    public YapiException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 379804795379140990L;

}