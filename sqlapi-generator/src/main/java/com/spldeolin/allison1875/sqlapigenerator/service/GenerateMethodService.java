package com.spldeolin.allison1875.sqlapigenerator.service;

import java.io.File;
import java.util.List;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;

/**
 * @author Deolin 2024-01-21
 */
public interface GenerateMethodService {

    List<FileFlush> generateMapperXmlMethod(List<File> mapperXmls);

    MapperMethodGenerationDto generateMapperMethod(CoidsOnTrackDto coidsOnTrack);

    ServiceMethodGenerationDto generateServiceMethod(CoidsOnTrackDto coidsOnTrack);

    ControllerGenerationDto generateControllerMethod(CoidsOnTrackDto coidsOnTrack);

}