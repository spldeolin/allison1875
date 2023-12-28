package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import javax.annotation.Nullable;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.service.impl.TransformParameterServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformParameterServiceImpl.class)
public interface TransformParameterService {

    @Nullable
    ParameterTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta, AstForest astForest,
            List<FileFlush> flushes);

}