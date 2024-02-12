package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.DescAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(DescAnalyzerServiceImpl.class)
public interface DescAnalyzerService {

    List<String> ananlyzeMethodDesc(MvcHandlerDto mvcHandler);

    List<String> ananlyzeFieldDesc(FieldDeclaration fd);

}