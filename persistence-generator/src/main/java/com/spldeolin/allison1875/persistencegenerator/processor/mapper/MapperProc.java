package com.spldeolin.allison1875.persistencegenerator.processor.mapper;

import java.util.Collection;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-13
 */
@Log4j2
public abstract class MapperProc {

    protected String calcMethodName(ClassOrInterfaceDeclaration mapper, String expectMethodName) {
        int v = 2;
        while (true) {
            if (existDeclared(mapper, expectMethodName)) {
                String newName = expectMethodName + "V" + v;
                log.warn("[{}]中已声明了的名为[{}]方法，待生成的方法重命名为[{}]", mapper.getNameAsString(), expectMethodName, newName);
                expectMethodName = newName;
                v++;
            } else {
                return expectMethodName;
            }
        }
    }

    protected boolean existDeclared(ClassOrInterfaceDeclaration mapper, String methodName) {
        List<MethodDeclaration> methods = mapper.getMethodsByName(methodName);
        for (MethodDeclaration method : methods) {
            Collection<String> descriptionLines = JavadocDescriptions.getEveryLine(method);
            if (descriptionLines.stream().anyMatch(line -> line.contains(BaseConstant.BY_ALLISON_1875))) {
                method.remove();
            }
        }
        return mapper.getMethodsByName(methodName).size() > 0;
    }

}