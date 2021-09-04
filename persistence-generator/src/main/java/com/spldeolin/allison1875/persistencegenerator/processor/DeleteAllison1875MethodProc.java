package com.spldeolin.allison1875.persistencegenerator.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;

/**
 * @author Deolin 2020-09-02
 */
@Singleton
public class DeleteAllison1875MethodProc {

    public void process(ClassOrInterfaceDeclaration mapper) {
        for (MethodDeclaration method : mapper.getMethods()) {
            boolean byAllison1875 = StringUtils.containsAny(JavadocDescriptions.getRaw(method),
                    BaseConstant.BY_ALLISON_1875, LotNo.NO_MANUAL_MODIFICATION);
            if (byAllison1875) {
                method.remove();
            }
        }
    }

}