package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.dto.KeyMethodNameDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByKeysDTO;
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

    List<String> generateBatchUpdateEvenNullMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateBatchUpdateMethod(TableAnalysisDTO persistence, String methodName);

    /**
     * 根据外键删除
     * 表中每有几个外键，这个方法就生成几个method，以_id结尾的字段算作外键
     */
    List<String> generateDeleteByKeyMethod(TableAnalysisDTO persistence,
            List<KeyMethodNameDTO> KeyAndMethodNames);

    List<String> generateInsertOrUpdateMethod(TableAnalysisDTO persistence, String entityName,
            String methodName);

    List<String> generateInsertMethod(TableAnalysisDTO persistence, String entityName, String methodName);

    List<String> generateListAllMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateQueryByEntityMethod(TableAnalysisDTO persistence, String entityName,
            String methodName);

    /**
     * 这个Proc生成2种方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    List<String> generateQueryByIdsMethod(TableAnalysisDTO persistence, String methodName);

    List<String> generateQueryByIdMethod(TableAnalysisDTO persistence, String methodName);

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    List<String> generateQueryByKeysMethod(TableAnalysisDTO persistence, List<QueryByKeysDTO> queryByKeysDTOs);

    List<String> generateQueryByKeyMethod(TableAnalysisDTO persistence,
            List<KeyMethodNameDTO> keyAndMethodNames);

    List<String> generateResultMap(TableAnalysisDTO persistence, String entityName);

    List<String> generateUpdateByIdEvenNullMethod(TableAnalysisDTO persistence, String entityName,
            String methodName);

    List<String> generateUpdateByIdMethod(TableAnalysisDTO persistence, String entityName, String methodName);

    FileFlush replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args);

}
