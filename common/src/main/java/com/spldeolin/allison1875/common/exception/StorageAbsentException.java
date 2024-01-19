package com.spldeolin.allison1875.common.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2020-02-03
 */
public class StorageAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = 5774104148448709313L;

    public StorageAbsentException(String message) {
        super(message);
    }

    public StorageAbsentException(Throwable cause) {
        super(cause);
    }

    public StorageAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}
