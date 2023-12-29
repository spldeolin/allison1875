package com.spldeolin.allison1875.base.util;

/**
 * @author Deolin 2023-05-23
 */
public class NamingUtils {

    private NamingUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String qualifierToTypeName(String qualifier) {
        int lastDotIndex = qualifier.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return qualifier.substring(lastDotIndex + 1);
        } else {
            return qualifier;
        }

    }

}