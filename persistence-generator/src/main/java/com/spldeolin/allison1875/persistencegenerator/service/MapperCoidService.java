package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.persistencegenerator.dto.DeleteByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.DetectOrGenerateMapperRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperCoidServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperCoidServiceImpl.class)
public interface MapperCoidService {

    DetectOrGenerateMapperRetval detectOrGenerateMapper(TableAnalysisDTO persistence,
            DataModelGeneration dataModelGeneration);

    String generateInsertMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchInsertMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchInsertEvenNullMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateUpdateByIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateUpdateByIdEvenNullMethodToMapper(GenerateMethodToMapperArgs args);

    String generateDeleteByIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdsMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdsEachIdMethodToMapper(GenerateMethodToMapperArgs args);

    QueryByIndexMethodDTO generateQueryByIndexMethodToMapper(GenerateMethodToMapperArgs args, List<PropertyDTO> keys,
            Boolean isUnique);

    DeleteByIndexMethodDTO generateDeleteByIndexMethodToMapper(GenerateMethodToMapperArgs args, List<PropertyDTO> keys);

    String generateListAllMethodToMapper(GenerateMethodToMapperArgs args);

}