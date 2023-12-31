package com.spldeolin.allison1875.persistencegenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.persistencegenerator.service.DeleteAllison1875MethodService;

/**
 * @author Deolin 2020-09-02
 */
@Singleton
public class DeleteAllison1875MethodServiceImpl implements DeleteAllison1875MethodService {

    @Override
    public void process(ClassOrInterfaceDeclaration mapper) {
        for (MethodDeclaration method : mapper.getMethods()) {
            boolean byAllison1875 = StringUtils.containsAny(JavadocDescriptions.getRaw(method),
                    BaseConstant.NO_MODIFY_ANNOUNCE);
            if (byAllison1875) {
                method.remove();
            }
        }
    }

}