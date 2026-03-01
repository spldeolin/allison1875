package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.querytransformer.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.querytransformer.dto.GenerateMethodToMapperXmlArgs;
import com.spldeolin.allison1875.querytransformer.service.impl.MapperLayerServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(MapperLayerServiceImpl.class)
public interface MapperLayerService {

    void generateMethodToMapper(GenerateMethodToMapperArgs args);

    void generateMethodToMapperXml(GenerateMethodToMapperXmlArgs args);

}