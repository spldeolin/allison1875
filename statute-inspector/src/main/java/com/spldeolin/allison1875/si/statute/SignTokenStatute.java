package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-27
 */
public class SignTokenStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> {
            cu.findAll(MethodCallExpr.class).forEach(mce -> mce.getScope().ifPresent(scope -> {
                if (scope.toString().equals("SecurityContextUtils") && mce.getNameAsString()
                        .equals("getCurrentUserDto")) {
                    result.add(new LawlessDto(mce).setMessage("任何地方禁止出现SecurityContextUtils.getCurrentUserDto"));

                }
            }));
            cu.findAll(MethodDeclaration.class, method -> !method.getAnnotationByName("PostMapping").isPresent())
                    .forEach(nonHandler -> nonHandler.findAll(MethodCallExpr.class)
                            .forEach(mce -> mce.getScope().ifPresent(scope -> {
                                if (scope.toString().equals("SecurityContextUtils")) {
                                    result.add(new LawlessDto(mce).setMessage("非Handler方法禁止出现SecurityContextUtils"));
                                }
                            })));
        });

        return result;
    }

}
