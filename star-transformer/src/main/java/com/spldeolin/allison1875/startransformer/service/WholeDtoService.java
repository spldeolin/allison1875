package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.GenerateWholeDtoServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(GenerateWholeDtoServiceImpl.class)
public interface WholeDtoService {

    JavabeanGeneration generateWholeDto(AstForest astForest, ChainAnalysisDto analysis);

}