package com.spldeolin.allison1875.sqlapigenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateControllerMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMapperMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.MethodLayerServiceImpl;

/**
 * @author Deolin 2024-01-21
 */
@ImplementedBy(MethodLayerServiceImpl.class)
public interface MethodGeneratorService {

    GenerateMapperMethodRetval generateMapperMethod(TrackCoidDto coidsOnTrack, AstForest astForest);

    List<String> generateMapperXmlMethod(GenerateMapperMethodRetval generateMapperMethodRetval);

    GenerateServiceMethodRetval generateServiceMethod(TrackCoidDto trackCoid,
            GenerateMapperMethodRetval generateMapperMethodRetval);

    GenerateControllerMethodRetval generateControllerMethod(TrackCoidDto trackCoid,
            GenerateServiceMethodRetval generateServiceMethodRetval, AstForest astForest);

}