package com.spldeolin.allison1875.base.util;

import java.util.Collection;

/**
 * @author Deolin 2020-11-09
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !(isEmpty(collection));
    }

}