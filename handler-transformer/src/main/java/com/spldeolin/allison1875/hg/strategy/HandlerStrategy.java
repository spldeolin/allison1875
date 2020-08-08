package com.spldeolin.allison1875.hg.strategy;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.hg.meta.HandlerMetaInfo;

/**
 * @author Deolin 2020-06-26
 */
public interface HandlerStrategy {

    Type resolveReturnType(HandlerMetaInfo metaInfo);

    BlockStmt resolveBody(HandlerMetaInfo metaInfo);

}
