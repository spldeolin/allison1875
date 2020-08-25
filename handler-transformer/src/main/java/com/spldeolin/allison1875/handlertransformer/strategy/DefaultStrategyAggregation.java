package com.spldeolin.allison1875.handlertransformer.strategy;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.spldeolin.allison1875.handlertransformer.meta.HandlerMetaInfo;

/**
 * @author Deolin 2020-06-26
 */
public class DefaultStrategyAggregation implements HandlerStrategy, PackageStrategy {

    @Override
    public Type resolveReturnType(HandlerMetaInfo metaInfo) {
        return StaticJavaParser.parseType(metaInfo.getRespBodyDto().typeName());
    }

    @Override
    public BlockStmt resolveBody(HandlerMetaInfo metaInfo) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement("return null;"));
        return body;
    }

    @Override
    public String calcDtoPackage(String controllerPackage) {
        String part = ".javabean.dto";
        if (controllerPackage.contains(".controller")) {
            String result = controllerPackage.substring(0, controllerPackage.length() - ".controller".length());
            result += part;
            return result;
        }
        return controllerPackage + part;
    }

    @Override
    public String calcReqPackage(String controllerPackage) {
        String part = ".javabean.req";
        if (controllerPackage.contains(".controller")) {
            String result = controllerPackage.substring(0, controllerPackage.length() - ".controller".length());
            result += part;
            return result;
        }
        return controllerPackage + part;
    }

    @Override
    public String calcRespPackage(String controllerPackage) {
        String part = ".javabean.resp";
        if (controllerPackage.contains(".controller")) {
            String result = controllerPackage.substring(0, controllerPackage.length() - ".controller".length());
            result += part;
            return result;
        }
        return controllerPackage + part;
    }

    @Override
    public String calcServicePackage(String controllerPackage) {
        String part = ".service";
        if (controllerPackage.contains(".controller")) {
            String result = controllerPackage.substring(0, controllerPackage.length() - ".controller".length());
            result += part;
            return result;
        }
        return controllerPackage + part;
    }

    @Override
    public String calcServiceImplPackage(String controllerPackage) {
        String part = ".service.impl";
        if (controllerPackage.contains(".controller")) {
            String result = controllerPackage.substring(0, controllerPackage.length() - ".controller".length());
            result += part;
            return result;
        }
        return controllerPackage + part;
    }

}
