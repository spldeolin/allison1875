package com.spldeolin.allison1875.docanalyzer.handle.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;

/**
 * @author Deolin 2020-06-02
 */
public class DefaultObtainConcernedResponseBodyHandle implements ObtainConcernedResponseBodyHandle {

    @Override
    public ResolvedType findConcernedResponseBodyType(MethodDeclaration handler) {
        return handler.getType().resolve();
    }

}
