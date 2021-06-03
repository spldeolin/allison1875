package com.spldeolin.allison1875.base.util;

import java.nio.charset.StandardCharsets;
import com.google.common.hash.Hashing;

/**
 * @author Deolin 2021-06-02
 */
public class HashUtil {

    private HashUtil() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String md5(String string) {
        return Hashing.hmacMd5("Allison 1875".getBytes(StandardCharsets.UTF_8))
                .hashString(string, StandardCharsets.UTF_8).toString();
    }

}