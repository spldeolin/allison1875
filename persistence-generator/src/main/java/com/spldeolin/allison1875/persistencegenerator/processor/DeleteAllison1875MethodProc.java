package com.spldeolin.allison1875.persistencegenerator.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;

/**
 * @author Deolin 2020-09-02
 */
class DeleteAllison1875MethodProc {

    private final ClassOrInterfaceDeclaration mapper;


    public DeleteAllison1875MethodProc(ClassOrInterfaceDeclaration mapper) {
        this.mapper = mapper;
    }

    void process() {
        for (MethodDeclaration method : mapper.getMethods()) {
            boolean byAllison1875 = JavadocDescriptions.getEveryLineInOne(method, ",")
                    .contains(BaseConstant.BY_ALLISON_1875);
            if (byAllison1875) {
                method.remove();
            }
        }
    }

}