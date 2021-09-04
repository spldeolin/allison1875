package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchInsertEvenNullProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchInsertProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchUpdateEvenNullProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchUpdateProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.DeleteByKeyProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.InsertOrUpdateProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.InsertProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.ListAllProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByEntityProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsEachIdProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByIdsProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeyProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.QueryByKeysProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdEvenNullProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.UpdateByIdProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.AllCloumnSqlXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.BatchInsertEvenNullXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.BatchInsertXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.BatchUpdateEvenNullXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.BatchUpdateXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.DeleteByKeyXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.InsertOrUpdateXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.InsertXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.ListAllXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.QueryByEntityXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.QueryByIdXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.QueryByIdsXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.QueryByKeyXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.QueryByKeysXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.ResultMapXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.UpdateByIdEvenNullXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.UpdateByIdXmlProc;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Singleton
@Log4j2
public class PersistenceGenerator implements Allison1875MainProcessor {

    @Inject
    private BatchInsertEvenNullProc batchInsertEvenNullProc;

    @Inject
    private BatchInsertEvenNullXmlProc batchInsertEvenNullXmlProc;

    @Inject
    private BatchUpdateProc batchUpdateProc;

    @Inject
    private BatchUpdateXmlProc batchUpdateXmlProc;

    @Inject
    private BatchUpdateEvenNullProc batchUpdateEvenNullProc;

    @Inject
    private BatchUpdateEvenNullXmlProc batchUpdateEvenNullXmlProc;

    @Inject
    private InsertProc insertProc;

    @Inject
    private BatchInsertProc batchInsertProc;

    @Inject
    private BatchInsertXmlProc batchInsertXmlProc;

    @Inject
    private InsertXmlProc insertXmlProc;

    @Inject
    private QueryByIdProc queryByIdProc;

    @Inject
    private QueryByIdXmlProc queryByIdXmlProc;

    @Inject
    private UpdateByIdProc updateByIdProc;

    @Inject
    private UpdateByIdXmlProc updateByIdXmlProc;

    @Inject
    private ResultMapXmlProc resultMapXmlProc;

    @Inject
    private AllCloumnSqlXmlProc allCloumnSqlXmlProc;

    @Inject
    private UpdateByIdEvenNullProc updateByIdEvenNullProc;

    @Inject
    private UpdateByIdEvenNullXmlProc updateByIdEvenNullXmlProc;

    @Inject
    private QueryByIdsProc queryByIdsProc;

    @Inject
    private QueryByIdsEachIdProc queryByIdsEachIdProc;

    @Inject
    private QueryByIdsXmlProc queryByIdsXmlProc;

    @Inject
    private QueryByKeyProc queryByKeyProc;

    @Inject
    private DeleteByKeyProc deleteByKeyProc;

    @Inject
    private QueryByKeysProc queryByKeysProc;

    @Inject
    private QueryByKeyXmlProc queryByKeyXmlProc;

    @Inject
    private DeleteByKeyXmlProc deleteByKeyXmlProc;

    @Inject
    private QueryByKeysXmlProc queryByKeysXmlProc;

    @Inject
    private QueryByEntityProc queryByEntityProc;

    @Inject
    private QueryByEntityXmlProc queryByEntityXmlProc;

    @Inject
    private ListAllProc listAllProc;

    @Inject
    private ListAllXmlProc listAllXmlProc;

    @Inject
    private InsertOrUpdateProc insertOrUpdateProc;

    @Inject
    private InsertOrUpdateXmlProc insertOrUpdateXmlProc;

    @Inject
    private BuildPersistenceDtoProc buildPersistenceDtoProc;

    @Inject
    private DeleteAllison1875MethodProc deleteAllison1875MethodProc;

    @Inject
    private FindOrCreateMapperProc findOrCreateMapperProc;

    @Inject
    private GenerateEntityProc entityProc;

    @Inject
    private MapperXmlProc mapperXmlProc;

    @Inject
    private GenerateDesignProc generateDesignProc;

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public void process(AstForest astForest) {
        // 构建并遍历 PersistenceDto对象
        Collection<PersistenceDto> persistenceDtos = buildPersistenceDtoProc.process(astForest);
        if (persistenceDtos.size() == 0) {
            log.warn("no tables detected in Schema [{}] at Connection [{}].", config.getSchema(), config.getJdbcUrl());
            return;
        }
        for (PersistenceDto persistence : persistenceDtos) {

            // 重新生成Entity
            EntityGeneration entityGeneration = entityProc.process(persistence, astForest);
            if (entityGeneration.isSameNameAndLotNoPresent()) {
                continue;
            }

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                mapper = findOrCreateMapperProc.process(persistence, entityGeneration, astForest);
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 重新生成QueryDesign
            generateDesignProc.process(persistence, entityGeneration, mapper, astForest);

            // 删除Mapper中所有Allison 1875生成的并且声明了不可人为修改的方法
            deleteAllison1875MethodProc.process(mapper);

            // 临时删除Mapper中所有开发者自定义方法
            List<MethodDeclaration> customMethods = mapper.getMethods();
            customMethods.forEach(MethodDeclaration::remove);

            // 在Mapper中生成基础方法
            String insertMethodName = insertProc.process(persistence, mapper);
            String batchInsertMethodName = batchInsertProc.process(persistence, mapper);
            String batchInsertEvenNullMethodName = batchInsertEvenNullProc.process(persistence, mapper);
            String batchUpdateMethodName = batchUpdateProc.process(persistence, mapper);
            String batchUpdateEvenNullMethodName = batchUpdateEvenNullProc.process(persistence, mapper);
            String queryByIdMethodName = queryByIdProc.process(persistence, mapper);
            String updateByIdMethodName = updateByIdProc.process(persistence, mapper);
            String updateByIdEvenNullMethodName = updateByIdEvenNullProc.process(persistence, mapper);
            String queryByIdsProcMethodName = queryByIdsProc.process(persistence, mapper);
            String queryByIdsEachIdMethodName = queryByIdsEachIdProc.process(persistence, mapper);
            Collection<KeyMethodNameDto> queryByKeyDtos = Lists.newArrayList();
            Collection<KeyMethodNameDto> deleteByKeyDtos = Lists.newArrayList();
            Collection<QueryByKeysDto> queryByKeysDtos = Lists.newArrayList();
            for (PropertyDto key : persistence.getKeyProperties()) {
                queryByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(queryByKeyProc.process(persistence, key, mapper)));
                deleteByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(deleteByKeyProc.process(persistence, key, mapper)));
                queryByKeysDtos.add(queryByKeysProc.process(persistence, key, mapper));
            }
            String queryByEntityMethodName = queryByEntityProc.process(persistence, mapper);
            String listAllMethodName = listAllProc.process(persistence, mapper);
            String insertOrUpdateMethodName = insertOrUpdateProc.process(persistence, mapper);

            // 将临时删除的开发者自定义方法添加到Mapper的最后
            customMethods.forEach(one -> mapper.getMembers().addLast(one));

            // 在Mapper.xml中覆盖生成基础方法
            String entityName = getEntityNameInXml(entityGeneration);
            try {
                Path mapperXmlDirectory = astForest.getAstForestRoot()
                        .resolve(persistenceGeneratorConfig.getMapperXmlDirectoryPath());
                mapperXmlProc.process(persistence, mapper, mapperXmlDirectory,
                        Lists.newArrayList(resultMapXmlProc.process(persistence, entityName),
                                allCloumnSqlXmlProc.process(persistence),
                                insertXmlProc.process(persistence, entityName, insertMethodName),
                                batchInsertXmlProc.process(persistence, batchInsertMethodName),
                                batchInsertEvenNullXmlProc.process(persistence, batchInsertEvenNullMethodName),
                                batchUpdateXmlProc.process(persistence, batchUpdateMethodName),
                                batchUpdateEvenNullXmlProc.process(persistence, batchUpdateEvenNullMethodName),
                                queryByIdXmlProc.process(persistence, queryByIdMethodName),
                                updateByIdXmlProc.process(persistence, entityName, updateByIdMethodName),
                                updateByIdEvenNullXmlProc.process(persistence, entityName,
                                        updateByIdEvenNullMethodName),
                                queryByIdsXmlProc.process(persistence, queryByIdsProcMethodName),
                                queryByIdsXmlProc.process(persistence, queryByIdsEachIdMethodName),
                                queryByKeyXmlProc.process(persistence, queryByKeyDtos),
                                deleteByKeyXmlProc.process(persistence, deleteByKeyDtos),
                                queryByKeysXmlProc.process(persistence, queryByKeysDtos),
                                queryByEntityXmlProc.process(persistence, entityName, queryByEntityMethodName),
                                listAllXmlProc.process(persistence, listAllMethodName),
                                insertOrUpdateXmlProc.process(persistence, entityName, insertOrUpdateMethodName)));
            } catch (Exception e) {
                log.error("写入Mapper.xml时发生异常 persistence={}", persistence, e);
            }
        }

        Saves.saveAll();
    }

    private String getEntityNameInXml(EntityGeneration entityGeneration) {
        if (config.getIsEntityUsingAlias()) {
            return entityGeneration.getEntityName();
        } else {
            return entityGeneration.getEntityQualifier();
        }
    }

}