package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.MapperXmlServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlServiceImpl.class)
public interface MapperXmlService {

    /**
     * <sql id="all"></sql> 标签
     */
    Collection<String> allCloumnSqlXml(PersistenceDto persistence);

    Collection<String> batchInsertEvenNullXml(PersistenceDto persistence, String methodName);

    Collection<String> batchInsertXml(PersistenceDto persistence, String methodName);

    Collection<String> batchUpdateEvenNullXml(PersistenceDto persistence, String methodName);

    Collection<String> batchUpdateXml(PersistenceDto persistence, String methodName);

    /**
     * 根据外键删除
     *
     * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
     */
    Collection<String> deleteByKeyXml(PersistenceDto persistence, Collection<KeyMethodNameDto> KeyAndMethodNames);

    Collection<String> insertOrUpdateXml(PersistenceDto persistence, String entityName, String methodName);

    Collection<String> insertXml(PersistenceDto persistence, String entityName, String methodName);

    Collection<String> listAllXml(PersistenceDto persistence, String methodName);

    Collection<String> queryByEntityXml(PersistenceDto persistence, String entityName, String methodName);

    /**
     * 这个Proc生成2中方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    Collection<String> queryByIdsXml(PersistenceDto persistence, String methodName);

    Collection<String> queryByIdXml(PersistenceDto persistence, String methodName);

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    Collection<String> queryByKeysXml(PersistenceDto persistence, Collection<QueryByKeysDto> queryByKeysDtos);

    Collection<String> queryByKeyXml(PersistenceDto persistence, Collection<KeyMethodNameDto> keyAndMethodNames);

    Collection<String> resultMapXml(PersistenceDto persistence, String entityName);

    Collection<String> updateByIdEvenNullXml(PersistenceDto persistence, String entityName, String methodName);

    Collection<String> updateByIdXml(PersistenceDto persistence, String entityName, String methodName);

}
