package com.spldeolin.allison1875.base.util.exception;

/**
 * 工具类Https遇到预想以外的情况或是内部异常时，
 * 会抛出这个异常，以交给调用方决定如何处理
 *
 * @author Deolin 2020-03-05
 * @see HttpUtils
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = -2702259454541236423L;

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    protected HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
