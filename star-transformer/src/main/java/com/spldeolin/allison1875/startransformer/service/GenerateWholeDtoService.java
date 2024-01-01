package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.service.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.GenerateWholeDtoServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateWholeDtoServiceImpl.class)
public interface GenerateWholeDtoService {

    JavabeanGeneration generate(AstForest astForest, StarAnalysisDto analysis);

}