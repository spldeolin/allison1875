package com.spldeolin.allison1875.handlertransformer.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-23
 */
@Singleton
@Log4j2
public class ParseFirstLineProc {

    public FirstLineDto parse(NodeList<Statement> statements) {
        FirstLineDto result = new FirstLineDto();
        for (Statement stmt : statements) {
            stmt.ifExpressionStmt(exprStmt -> exprStmt.getExpression().ifVariableDeclarationExpr(vde -> {
                for (VariableDeclarator vd : vde.getVariables()) {
                    if (vd.getInitializer().isPresent()) {
                        Expression i = vd.getInitializer().get();
                        if (vd.getNameAsString().equals("handler")) {
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
                        if (vd.getNameAsString().equals("desc")) {
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
                    }
                }
            }));
        }
        if (StringUtils.isBlank(result.getHandlerUrl())) {
            return null;
        }
        result.setHandlerName(MoreStringUtils.slashToLowerCamel(result.getHandlerUrl()));
        return result;
    }

}