package com.spldeolin.allison1875.base.util.ast;

import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.google.common.base.Strings;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRanges {

    private TokenRanges() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static String getRawCode(Node node) {
        return node.getTokenRange().orElseThrow(() -> new RuntimeException("Token Range absent")).toString();
    }

    public static String getStartIndent(Node node) {
        TokenRange tokenRange = node.getTokenRange().orElseThrow(() -> new RuntimeException("Token Range absent"));
        Range range = tokenRange.getBegin().getRange().orElseThrow(() -> new RuntimeException("Range absent"));
        return Strings.repeat(" ", range.begin.column - 1);
    }

}