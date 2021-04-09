package com.spldeolin.allison1875.handlertransformer.handle;

import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * @author Deolin 2020-12-23
 */
public interface FirstLineMoreParseHandle {

    Pair<String, Object> parseMore(VariableDeclarator vd);

}
