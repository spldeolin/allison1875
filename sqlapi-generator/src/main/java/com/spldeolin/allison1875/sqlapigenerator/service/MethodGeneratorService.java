package com.spldeolin.allison1875.sqlapigenerator.service;

import java.util.List;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMapperMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceImplMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.MethodGeneratorServiceImpl;

/**
 * @author Deolin 2024-01-21
 */
@ImplementedBy(MethodGeneratorServiceImpl.class)
public interface MethodGeneratorService {

    GenerateMapperMethodRetval generateMapperMethod(TrackCoidDto coidsOnTrack, AstForest astForest);

    List<String> generateMapperXmlMethod(GenerateMapperMethodRetval generateMapperMethodRetval);

    GenerateServiceMethodRetval generateServiceMethod(TrackCoidDto trackCoid, MethodDeclaration mapperMethod);

    GenerateServiceImplMethodRetval generateServiceImplMethod(String mapperVarName,
            MethodDeclaration clonedServiceMethod, MethodDeclaration mapperMethod);

}