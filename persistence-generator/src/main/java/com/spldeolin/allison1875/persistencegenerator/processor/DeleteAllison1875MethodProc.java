package com.spldeolin.allison1875.persistencegenerator.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;

/**
 * @author Deolin 2020-09-02
 */
public class DeleteAllison1875MethodProc {

    public void process(ClassOrInterfaceDeclaration mapper) {
        for (MethodDeclaration method : mapper.getMethods()) {
            boolean byAllison1875 = JavadocDescriptions.getRaw(method).contains(BaseConstant.BY_ALLISON_1875);
            if (byAllison1875) {
                method.remove();
            }
        }
    }

}