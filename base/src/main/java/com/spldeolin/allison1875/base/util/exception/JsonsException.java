package com.spldeolin.allison1875.base.util.exception;

import com.spldeolin.allison1875.base.util.JsonUtils;

/**
 * 工具类Jsons遇到预想以外的情况或是内部异常时，
 * 会抛出这个异常，以交给调用方决定如何处理
 *
 * @author Deolin 2020-03-05
 * @see JsonUtils
 */
public class JsonsException extends RuntimeException {

    private static final long serialVersionUID = 2506389302288058433L;

    public JsonsException() {
        super();
    }

    public JsonsException(String message) {
        super(message);
    }

    public JsonsException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonsException(Throwable cause) {
        super(cause);
    }

    protected JsonsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
