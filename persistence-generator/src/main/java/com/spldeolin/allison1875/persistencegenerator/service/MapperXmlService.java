package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperXmlServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlServiceImpl.class)
public interface MapperXmlService {

    List<String> generateAllCloumnSql(TableStructureAnalysisDto persistence);

    List<String> generateBatchInsertEvenNullMethod(TableStructureAnalysisDto persistence, String methodName);

    List<String> generateBatchInsertMethod(TableStructureAnalysisDto persistence, String methodName);

    List<String> generateBatchUpdateEvenNullMethod(TableStructureAnalysisDto persistence, String methodName);

    List<String> generateBatchUpdateMethod(TableStructureAnalysisDto persistence, String methodName);

    /**
     * 根据外键删除
     * 表中每有几个外键，这个方法就生成几个method，以_id结尾的字段算作外键
     */
    List<String> generateDeleteByKeyMethod(TableStructureAnalysisDto persistence,
            List<KeyMethodNameDto> KeyAndMethodNames);

    List<String> generateInsertOrUpdateMethod(TableStructureAnalysisDto persistence, String entityName,
            String methodName);

    List<String> generateInsertMethod(TableStructureAnalysisDto persistence, String entityName, String methodName);

    List<String> generateListAllMethod(TableStructureAnalysisDto persistence, String methodName);

    List<String> generateQueryByEntityMethod(TableStructureAnalysisDto persistence, String entityName,
            String methodName);

    /**
     * 这个Proc生成2种方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    List<String> generateQueryByIdsMethod(TableStructureAnalysisDto persistence, String methodName);

    List<String> generateQueryByIdMethod(TableStructureAnalysisDto persistence, String methodName);

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    List<String> generateQueryByKeysMethod(TableStructureAnalysisDto persistence, List<QueryByKeysDto> queryByKeysDtos);

    List<String> generateQueryByKeyMethod(TableStructureAnalysisDto persistence,
            List<KeyMethodNameDto> keyAndMethodNames);

    List<String> generateResultMap(TableStructureAnalysisDto persistence, String entityName);

    List<String> generateUpdateByIdEvenNullMethod(TableStructureAnalysisDto persistence, String entityName,
            String methodName);

    List<String> generateUpdateByIdMethod(TableStructureAnalysisDto persistence, String entityName, String methodName);

    FileFlush replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args);

}
