package com.spldeolin.allison1875.startransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.TransformWholeDtoServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformWholeDtoServiceImpl.class)
public interface TransformWholeDtoService {

    CompilationUnit transformWholeDto(JavabeanArg javabeanArg, AstForest astForest, ChainAnalysisDto analysis);

}