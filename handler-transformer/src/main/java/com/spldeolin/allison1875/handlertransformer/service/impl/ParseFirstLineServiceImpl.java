package com.spldeolin.allison1875.handlertransformer.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.util.HashingUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.ParseFirstLineService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-23
 */
@Singleton
@Log4j2
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
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i.toString());
                                }
                            } else {
                                log.warn("'handler' [{}] is not String Literal, ignore.", i.toString());
                            }
                        }
                        if (StringUtils.equalsAny(vd.getNameAsString(), "desc", "d")) {
                            if (i.isStringLiteralExpr()) {
                                if (result.getHandlerDescription() == null) {
                                    result.setHandlerDescription(i.asStringLiteralExpr().getValue());
                                } else {
                                    log.warn("'handler' [{}] duplicate declaration, ignore.", i.toString());
                                }
                            } else {
                                log.warn("'desc' [{}] is not String Literal, ignore.", i.toString());
                            }
                        }
                        if (StringUtils.equalsAny(vd.getNameAsString(), "service", "s")) {
                            if (i.isClassExpr()) {
                                result.setPresentServiceQualifier(i.asClassExpr().getType().resolve().describe());
                            } else if (i.isStringLiteralExpr()) {
                                result.setServiceName(i.asStringLiteralExpr().getValue());
                            } else {
                                log.warn("'service' [{}] is not String Literal nor Class Expression, ignore.",
                                        i.toString());
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