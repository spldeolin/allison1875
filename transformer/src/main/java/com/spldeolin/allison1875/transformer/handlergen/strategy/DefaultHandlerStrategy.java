package com.spldeolin.allison1875.transformer.handlergen.strategy;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.transformer.handlergen.HandlerMetaInfo;

/**
 * @author Deolin 2020-06-26
 */
public class DefaultHandlerStrategy implements HandlerStrategy {

    @Override
    public Type resolveReturnType(HandlerMetaInfo metaInfo) {
        return StaticJavaParser.parseType(metaInfo.respBodyDto().typeName());
    }

    @Override
    public BlockStmt resolveBody(HandlerMetaInfo metaInfo) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement("return null;"));
        return body;
    }

}
