package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.ParseFirstLineServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ParseFirstLineServiceImpl.class)
public interface ParseFirstLineService {

    FirstLineDto parse(InitializerDeclaration init, CompilationUnit controllerCu);

}