package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.DescAnalyzerService;

/**
 * @author Deolin 2021-04-06
 */
@Singleton
public class DescAnalyzerServiceImpl implements DescAnalyzerService {

    @Override
    public List<String> ananlyzeMethodDesc(MvcHandlerDto mvcHandler) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getCommentAsLines(mvcHandler.getMethodDec());
    }

    @Override
    public List<String> ananlyzeFieldDesc(FieldDeclaration fd) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getCommentAsLines(fd);
    }

}