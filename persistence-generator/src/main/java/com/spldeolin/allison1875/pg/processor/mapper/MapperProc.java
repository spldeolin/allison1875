package com.spldeolin.allison1875.pg.processor.mapper;

import java.util.Collection;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-08-13
 */
@Slf4j
public abstract class MapperProc {

    protected boolean existDeclared(ClassOrInterfaceDeclaration mapper, String methodName) {
        List<MethodDeclaration> methods = mapper.getMethodsByName(methodName);
        for (MethodDeclaration method : methods) {
            Collection<String> descriptionLines = JavadocDescriptions.getEveryLine(method);
            if (descriptionLines.stream().anyMatch(line -> line.contains(BaseConstant.BY_ALLISON_1875))) {
                method.remove();
            }
        }
        if (mapper.getMethodsByName(methodName).size() > 0) {
            log.warn("用户已在[{}]中声明了的名为[{}]方法，不再覆盖生成", mapper.getNameAsString(), methodName);
            return true;
        }
        return false;
    }

}