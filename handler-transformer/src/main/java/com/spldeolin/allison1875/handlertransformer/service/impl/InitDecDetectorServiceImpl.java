package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.service.InitDecDetectorService;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
public class InitDecDetectorServiceImpl implements InitDecDetectorService {

    @Override
    public List<InitializerDeclaration> detectInitDecs(ClassOrInterfaceDeclaration mvcController) {
        List<InitializerDeclaration> result = Lists.newArrayList();
        for (BodyDeclaration<?> member : mvcController.getMembers()) {
            if (member.isInitializerDeclaration()) {
                result.add(member.asInitializerDeclaration());
            }
        }
        return result;
    }

}