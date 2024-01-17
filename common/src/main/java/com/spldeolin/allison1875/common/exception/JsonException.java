package com.spldeolin.allison1875.common.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;

/**
 * 工具类Jsons遇到预想以外的情况或是内部异常时，
 * 会抛出这个异常，以交给调用方决定如何处理
 *
 * @author Deolin 2020-03-05
 * @see JsonUtils
 */
public class JsonException extends Allison1875Exception {

    private static final long serialVersionUID = 2506389302288058433L;

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }

}
