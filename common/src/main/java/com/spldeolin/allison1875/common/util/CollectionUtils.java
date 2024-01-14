package com.spldeolin.allison1875.common.util;

import java.util.List;

/**
 * @author Deolin 2020-11-09
 */
public class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static boolean isEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(List<?> collection) {
        return !(isEmpty(collection));
    }

}