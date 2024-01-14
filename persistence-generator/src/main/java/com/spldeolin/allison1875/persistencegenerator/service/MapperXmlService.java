package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperXmlServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlServiceImpl.class)
public interface MapperXmlService {

    /**
     * <sql id="all"></sql> 标签
     */
    List<String> allCloumnSqlXml(PersistenceDto persistence);

    List<String> batchInsertEvenNullXml(PersistenceDto persistence, String methodName);

    List<String> batchInsertXml(PersistenceDto persistence, String methodName);

    List<String> batchUpdateEvenNullXml(PersistenceDto persistence, String methodName);

    List<String> batchUpdateXml(PersistenceDto persistence, String methodName);

    /**
     * 根据外键删除
     *
     * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
     */
    List<String> deleteByKeyXml(PersistenceDto persistence, List<KeyMethodNameDto> KeyAndMethodNames);

    List<String> insertOrUpdateXml(PersistenceDto persistence, String entityName, String methodName);

    List<String> insertXml(PersistenceDto persistence, String entityName, String methodName);

    List<String> listAllXml(PersistenceDto persistence, String methodName);

    List<String> queryByEntityXml(PersistenceDto persistence, String entityName, String methodName);

    /**
     * 这个Proc生成2中方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    List<String> queryByIdsXml(PersistenceDto persistence, String methodName);

    List<String> queryByIdXml(PersistenceDto persistence, String methodName);

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    List<String> queryByKeysXml(PersistenceDto persistence, List<QueryByKeysDto> queryByKeysDtos);

    List<String> queryByKeyXml(PersistenceDto persistence, List<KeyMethodNameDto> keyAndMethodNames);

    List<String> resultMapXml(PersistenceDto persistence, String entityName);

    List<String> updateByIdEvenNullXml(PersistenceDto persistence, String entityName, String methodName);

    List<String> updateByIdXml(PersistenceDto persistence, String entityName, String methodName);

}
