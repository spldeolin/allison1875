package com.spldeolin.allison1875.querytransformer.util;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRanges {

    private TokenRanges() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String getRawCode(Node node) {
        if (node.getComment().isPresent()) {
            JavaToken begin = node.getComment().get().getTokenRange()
                    .orElseThrow(() -> new RuntimeException("Token Range absent")).getBegin();
            JavaToken end = node.getTokenRange().orElseThrow(() -> new RuntimeException("Token Range absent")).getEnd();
            return new TokenRange(begin, end).toString();
        } else {
            return node.getTokenRange().orElseThrow(() -> new RuntimeException("Token Range absent")).toString();
        }
    }

}