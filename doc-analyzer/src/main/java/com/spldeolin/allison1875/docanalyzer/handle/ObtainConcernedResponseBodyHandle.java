package com.spldeolin.allison1875.docanalyzer.handle;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Singleton;

/**
 * @author Deolin 2020-06-02
 */
@Singleton
public class ObtainConcernedResponseBodyHandle {

    public ResolvedType findConcernedResponseBodyType(MethodDeclaration handler) {
        return handler.getType().resolve();
    }

}
