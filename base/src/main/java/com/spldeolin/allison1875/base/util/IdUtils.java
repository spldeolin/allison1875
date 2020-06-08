package com.spldeolin.allison1875.base.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Deolin 2020-06-08
 */
public class IdUtils {

    private static final AtomicLong atomicLong = new AtomicLong(1);

    public static Long nextId() {
        return atomicLong.getAndAdd(1);
    }

}
