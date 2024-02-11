package com.spldeolin.allison1875.querytransformer.util;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.querytransformer.exception.TokenRangeAbsentException;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRangeUtils {

    private TokenRangeUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String getRawCode(Node node) {
        if (node.getComment().isPresent()) {
            JavaToken begin = node.getComment().get().getTokenRange()
                    .orElseThrow(() -> new TokenRangeAbsentException(node)).getBegin();
            JavaToken end = node.getTokenRange().orElseThrow(() -> new TokenRangeAbsentException(node)).getEnd();
            return new TokenRange(begin, end).toString();
        } else {
            return node.getTokenRange().orElseThrow(() -> new TokenRangeAbsentException(node)).toString();
        }
    }

}