package com.spldeolin.allison1875.docanalyzer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.SimplyAnalyzeServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(SimplyAnalyzeServiceImpl.class)
public interface SimplyAnalyzeService {

    void process(ClassOrInterfaceDeclaration controller, HandlerFullDto handler, EndpointDto endpoint);

}