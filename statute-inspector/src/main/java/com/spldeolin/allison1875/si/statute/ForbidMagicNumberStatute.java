package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-28
 */
public class ForbidMagicNumberStatute implements Statute {


    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {
            method.findAll(LiteralStringValueExpr.class).forEach(lsv -> {
                if (lsv.isIntegerLiteralExpr() || lsv.isDoubleLiteralExpr() || lsv.isLongLiteralExpr()) {
                    result.add(new LawlessDto(lsv).setMessage("禁止使用魔法数字"));
                }
                lsv.ifStringLiteralExpr(sle -> {
                    if (StringUtils.isNumeric(sle.asString())) {
                        result.add(new LawlessDto(lsv).setMessage("禁止使用内容是纯数字的魔法字符串"));
                    }
                });
            });
        }));

        return result;
    }

}
