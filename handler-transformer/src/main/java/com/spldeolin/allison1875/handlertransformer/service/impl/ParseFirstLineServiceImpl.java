package com.spldeolin.allison1875.handlertransformer.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.ParseFirstLineService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-23
 */
@Singleton
@Slf4j
public class ParseFirstLineServiceImpl implements ParseFirstLineService {

    @Override
    public FirstLineDto parse(InitializerDeclaration init) {
        FirstLineDto result = new FirstLineDto();
        result.setInit(init);
        for (Statement stmt : init.getBody().getStatements()) {
            stmt.ifExpressionStmt(exprStmt -> exprStmt.getExpression().ifVariableDeclarationExpr(vde -> {
                for (VariableDeclarator vd : vde.getVariables()) {
                    if (vd.getInitializer().isPresent()) {
                        Expression i = vd.getInitializer().get();
                        if (StringUtils.equalsAny(vd.getNameAsString(), "handler", "h")) {
                            if (i.isStringLiteralExpr()) {
                                if (result.getHandlerUrl() == null) {
                                    result.setHandlerUrl(i.asStringLiteralExpr().getValue());
                                } else {
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i);
                                }
                            } else {
                                log.warn("'handler' [{}] is not String Literal, ignore.", i);
                            }
                        }
                        if (StringUtils.equalsAny(vd.getNameAsString(), "desc", "d")) {
                            if (i.isStringLiteralExpr()) {
                                if (result.getHandlerDescription() == null) {
                                    result.setHandlerDescription(i.asStringLiteralExpr().getValue());
                                } else {
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i);
                                }
                            } else {
                                log.warn("'desc' [{}] is not String Literal, ignore.", i);
                            }
                        }
                    }
                }
            }));
        }
        if (StringUtils.isBlank(result.getHandlerUrl())) {
            log.warn("'handler' [{}] is blank, ignore", result.getHandlerUrl());
            return null;
        }
        if (StringUtils.isBlank(result.getHandlerDescription())) {
            result.setHandlerDescription("未指定描述");
        }
        result.setHandlerName(MoreStringUtils.slashToLowerCamel(result.getHandlerUrl()));
        String hash = StringUtils.upperCase(HashingUtils.hashString(JsonUtils.toJson(result)));
        result.setLotNo(String.format("HT%s-%s", Allison1875.SHORT_VERSION, hash));
        return result;
    }

}