package com.spldeolin.allison1875.inspector.statute;

import java.io.Serializable;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;

/**
 * @author Deolin 2020-08-31
 */
public class NumberMagicValue implements Statute, Serializable {

    private static final long serialVersionUID = -2193057433113496085L;

    @Override
    public Collection<LawlessDto> inspect(CompilationUnit cu) {
        Collection<LawlessDto> result = Lists.newArrayList();
        for (BodyDeclaration<?> bodyDeclaration : cu.findAll(BodyDeclaration.class)) {
            if (bodyDeclaration.isInitializerDeclaration() || bodyDeclaration.isCallableDeclaration()) {
                for (LiteralExpr literalExpr : bodyDeclaration.findAll(LiteralExpr.class)) {
                    if (literalExpr.isDoubleLiteralExpr() || literalExpr.isIntegerLiteralExpr() || literalExpr
                            .isLongLiteralExpr()) {
                        result.add(
                                new LawlessDto(literalExpr, null, String.format("出现了魔法值[%s]", literalExpr.toString())));
                    }
                }
            }

        }

        return result;

    }

}