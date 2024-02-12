package com.spldeolin.allison1875.docanalyzer.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.ConcernedResponseBodyAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ConcernedResponseBodyAnalyzerServiceImpl.class)
public interface ConcernedResponseBodyAnalyzerService {

    ResolvedType analyzeConcernedResponseBodyType(MethodDeclaration mvcHandlerMd);

}
