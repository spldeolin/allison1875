package com.spldeolin.allison1875.common.util;

import java.nio.charset.StandardCharsets;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.hash.Hashing;

/**
 * @author Deolin 2021-08-30
 */
public class HashingUtils {

    private HashingUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String hashString(String string) {
        return Hashing.murmur3_32().hashString(string, StandardCharsets.UTF_8).toString();
    }

    public static String hashTypeDeclaration(TypeDeclaration<?> typeDeclaration) {
        return Hashing.murmur3_128().hashString(
                MoreStringUtils.compressConsecutiveSpaces(MoreStringUtils.removeNewLine(typeDeclaration.toString())),
                StandardCharsets.UTF_8).toString();
    }

}