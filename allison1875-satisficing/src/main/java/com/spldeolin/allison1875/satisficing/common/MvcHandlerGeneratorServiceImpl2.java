package com.spldeolin.allison1875.satisficing.common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.impl.MvcHandlerGeneratorServiceImpl;

/**
 * @author Deolin 2024-02-17
 */
public class MvcHandlerGeneratorServiceImpl2 extends MvcHandlerGeneratorServiceImpl {

    @Override
    public GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args) {
        GenerateMvcHandlerRetval result = super.generateMvcHandler(args);
        Type returnType = result.getMvcHandler().getType();
        if (returnType.isVoidType()) {
            returnType.replace(
                    StaticJavaParser.parseType("com.spldeolin.satisficing.client.javabean.RequestResult<Void>"));
            result.getMvcHandler().getBody().get().getStatements()
                    .add(StaticJavaParser.parseStatement("return RequestResult.success();"));
        } else {
            returnType.replace(StaticJavaParser.parseType(
                    String.format("com.spldeolin.satisficing.client.javabean.RequestResult<%s>", returnType)));
            Expression returnExpr = result.getMvcHandler().getBody().get().getStatements().get(0).asReturnStmt()
                    .getExpression().get();
            returnExpr.replace(
                    StaticJavaParser.parseExpression(String.format("RequestResult.success(%s)", returnExpr)));
        }
        return result;
    }

}