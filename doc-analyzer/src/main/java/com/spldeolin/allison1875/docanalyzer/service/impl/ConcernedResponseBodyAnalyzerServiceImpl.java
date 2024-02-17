package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.service.ConcernedResponseBodyAnalyzerService;

/**
 * @author Deolin 2020-06-02
 */
@Singleton
public class ConcernedResponseBodyAnalyzerServiceImpl implements ConcernedResponseBodyAnalyzerService {

    @Override
    public ResolvedType analyzeConcernedResponseBodyType(MethodDeclaration mvcHandlerMd) {
        // 可拓展为分析出统一返回类型中的data部分类型等
        return mvcHandlerMd.getType().resolve();
    }

}
