package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.dto.DeleteByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperXmlServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlServiceImpl.class)
public interface MapperXmlService {

    List<String> generateAllCloumnSql(TableAnalysisDTO persistence);

    List<String> generateBatchInsertEvenNullMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateBatchInsertMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateInsertMethod(TableAnalysisDTO persistence, String entityName, String methodName);

    List<String> generateListAllMethod(TableAnalysisDTO persistence, String methodName);

    /**
     * 这个Proc生成2种方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    List<String> generateQueryByIdsMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateQueryByIdMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateResultMap(TableAnalysisDTO persistence, String entityName);

    List<String> generateUpdateByIdEvenNullMethod(TableAnalysisDTO persistence, String entityName, String methodName);

    List<String> generateUpdateByIdMethod(TableAnalysisDTO persistence, String entityName, String methodName);

    void replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args);

    List<String> generateQueryByIndexMethod(TableAnalysisDTO tableAnalysis,
            List<QueryByIndexMethodDTO> queryByIndexMethodNames);

    List<String> generateDeleteByIdMethod(TableAnalysisDTO tableAnalysis, String deleteByIdMethodName);

    List<String> generateDeleteByIndexMethod(TableAnalysisDTO tableAnalysis,
            List<DeleteByIndexMethodDTO> deleteByIndexMethodNames);

    List<String> generateQueryByBizIdsMethod(TableAnalysisDTO tableAnalysis,
            List<QueryByIndexMethodDTO> queryByBizIdsMethodNames);

}
