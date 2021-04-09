package com.spldeolin.allison1875.docanalyzer.handle;

import javax.inject.Singleton;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-06-02
 */
@Singleton
public class ObtainConcernedResponseBodyHandle {

    public ResolvedType findConcernedResponseBodyType(MethodDeclaration handler) {
        return handler.getType().resolve();
    }

}
