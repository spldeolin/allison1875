package com.spldeolin.allison1875.base.util.exception;

import com.spldeolin.allison1875.base.util.CsvUtils;

/**
 * 工具类Csvs遇到预想以外的情况或是内部异常时，
 * 会抛出这个异常，以交给调用方决定如何处理
 *
 * @author Deolin 2020-03-04
 * @see CsvUtils
 */
public class CsvsException extends RuntimeException {

    private static final long serialVersionUID = 8247500390534094194L;

    public CsvsException() {
        super();
    }

    public CsvsException(String message) {
        super(message);
    }

    public CsvsException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvsException(Throwable cause) {
        super(cause);
    }

    protected CsvsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
