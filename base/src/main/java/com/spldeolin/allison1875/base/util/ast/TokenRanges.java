package com.spldeolin.allison1875.base.util.ast;

import com.github.javaparser.ast.Node;

/**
 * @author Deolin 2021-06-14
 */
public class TokenRanges {

    public static String getRawCode(Node node) {
        return node.getTokenRange().orElseThrow(() -> new RuntimeException("Token Range absent")).toString();
    }

}