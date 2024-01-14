package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.service.InitializerCollectService;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
public class InitializerCollectServiceImpl implements InitializerCollectService {

    @Override
    public List<InitializerDeclaration> collectInitializer(ClassOrInterfaceDeclaration coid) {
        List<InitializerDeclaration> result = Lists.newArrayList();
        for (BodyDeclaration<?> member : coid.getMembers()) {
            if (member.isInitializerDeclaration()) {
                result.add(member.asInitializerDeclaration());
            }
        }
        return result;
    }

}