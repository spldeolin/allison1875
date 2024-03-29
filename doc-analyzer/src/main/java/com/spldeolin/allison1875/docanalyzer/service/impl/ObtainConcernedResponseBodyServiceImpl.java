package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.service.ObtainConcernedResponseBodyService;

/**
 * @author Deolin 2020-06-02
 */
@Singleton
public class ObtainConcernedResponseBodyServiceImpl implements ObtainConcernedResponseBodyService {

    @Override
    public ResolvedType findConcernedResponseBodyType(MethodDeclaration handler) {
        return handler.getType().resolve();
    }

}
