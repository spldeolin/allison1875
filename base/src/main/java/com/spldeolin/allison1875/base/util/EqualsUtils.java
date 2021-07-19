package com.spldeolin.allison1875.base.util;

import java.util.Objects;

/**
 * @author Deolin 2021-07-19
 */
public class EqualsUtils {

    private EqualsUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static boolean equalsAny(Object one, Object... others) {
        for (Object other : others) {
            boolean equals = Objects.equals(one, other);
            if (equals) {
                return true;
            }
        }
        return false;
    }

}