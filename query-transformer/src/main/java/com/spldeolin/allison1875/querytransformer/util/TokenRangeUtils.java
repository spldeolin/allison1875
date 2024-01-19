package com.spldeolin.allison1875.querytransformer.util;

import java.util.Optional;
import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRangeUtils {

    private TokenRangeUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String getRawCode(Node node) {
        Optional<TokenRange> tokenRange = node.getComment().get().getTokenRange();
        if (!tokenRange.isPresent()) {
            return "";
        }
        if (node.getComment().isPresent()) {
            JavaToken begin = tokenRange.get().getBegin();
            JavaToken end = tokenRange.get().getEnd();
            return new TokenRange(begin, end).toString();
        } else {
            return tokenRange.get().toString();
        }
    }

}