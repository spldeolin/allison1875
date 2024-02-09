package com.spldeolin.allison1875.sqlapigenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.GenerateMethodServiceImpl;

/**
 * @author Deolin 2024-01-21
 */
@ImplementedBy(GenerateMethodServiceImpl.class)
public interface GenerateMethodService {

    List<String> generateMapperXmlMethod(MapperMethodGenerationDto mapperMethodGeneration);

    MapperMethodGenerationDto generateMapperMethod(CoidsOnTrackDto coidsOnTrack, AstForest astForest);

    ServiceMethodGenerationDto generateServiceMethod(CoidsOnTrackDto coidsOnTrack,
            MapperMethodGenerationDto mapperMethodGeneration);

    ControllerMethodGenerationDto generateControllerMethod(CoidsOnTrackDto coidsOnTrack,
            ServiceMethodGenerationDto serviceMethodGeneration, AstForest astForest);

}