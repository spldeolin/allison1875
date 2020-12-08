package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.FindOrCreateMapperResultDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateEntityResultDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PathDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.BatchInsertEvenNullProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.DeleteByKeyProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapper.InsertProc;
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
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.DeleteByKeyXmlProc;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.InsertXmlProc;
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
public class PersistenceGenerator implements
        Allison1875MainProcessor<PersistenceGeneratorConfig, PersistenceGenerator> {

    @Inject
    private BatchInsertEvenNullProc batchInsertEvenNullProc;

    @Inject
    private BatchInsertEvenNullXmlProc batchInsertEvenNullXmlProc;

    @Inject
    private InsertProc insertProc;

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
    private BuildPersistenceDtoProc buildPersistenceDtoProc;

    @Inject
    private DeleteAllison1875MethodProc deleteAllison1875MethodProc;

    @Inject
    private FindOrCreateMapperProc findOrCreateMapperProc;

    @Inject
    private GenerateEntityProc entityProc;

    @Inject
    private PathProc pathProc;

    @Inject
    private MapperXmlProc mapperXmlProc;

    @Inject
    private GenerateQueryDesignProc generateQueryDesignProc;

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public PersistenceGenerator config(PersistenceGeneratorConfig config) {
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);
        PathDto pathDto = pathProc.process(astForest);

        Collection<CompilationUnit> toSave = Lists.newArrayList();

        // 构建并遍历 PersistenceDto对象
        for (PersistenceDto persistence : buildPersistenceDtoProc.process()) {

            // 重新生成Entity
            GenerateEntityResultDto generateEntityResult = entityProc.process(persistence, pathDto);
            CuCreator entityCuCreator = generateEntityResult.getEntityCuCreator();
            toSave.addAll(generateEntityResult.getToCreate());

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                FindOrCreateMapperResultDto findOrCreateMapperResult = findOrCreateMapperProc
                        .process(persistence, entityCuCreator);
                mapper = findOrCreateMapperResult.getMapper();
                toSave.add(findOrCreateMapperResult.getCu());
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 重新生成QueryDesign
            toSave.addAll(generateQueryDesignProc.process(persistence, entityCuCreator, mapper));

            // 删除Mapper中所有Allison 1875生成的方法
            deleteAllison1875MethodProc.process(mapper);

            // 在Mapper中生成基础方法
            String insertMethodName = insertProc.process(persistence, mapper);
            String batchInsertEvenNullMethodName = batchInsertEvenNullProc.process(persistence, mapper);
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

            // 在Mapper.xml中覆盖生成基础方法
            String entityName = getEntityNameInXml(entityCuCreator);
            try {
                mapperXmlProc.process(persistence, mapper, pathDto.getMapperXmlPath(),
                        Lists.newArrayList(resultMapXmlProc.process(persistence, entityName),
                                allCloumnSqlXmlProc.process(persistence),
                                insertXmlProc.process(persistence, entityName, insertMethodName),
                                batchInsertEvenNullXmlProc.process(persistence, batchInsertEvenNullMethodName),
                                queryByIdXmlProc.process(persistence, queryByIdMethodName),
                                updateByIdXmlProc.process(persistence, entityName, updateByIdMethodName),
                                updateByIdEvenNullXmlProc
                                        .process(persistence, entityName, updateByIdEvenNullMethodName),
                                queryByIdsXmlProc.process(persistence, queryByIdsProcMethodName),
                                queryByIdsXmlProc.process(persistence, queryByIdsEachIdMethodName),
                                queryByKeyXmlProc.process(persistence, queryByKeyDtos),
                                deleteByKeyXmlProc.process(persistence, deleteByKeyDtos),
                                queryByKeysXmlProc.process(persistence, queryByKeysDtos),
                                queryByEntityXmlProc.process(persistence, entityName, queryByEntityMethodName)));
            } catch (Exception e) {
                log.error("写入Mapper.xml时发生异常 persistence={}", persistence, e);
            }
        }

        toSave.forEach(Saves::save);
    }

    private String getEntityNameInXml(CuCreator entityCuCreator) {
        if (config.getIsEntityUsingAlias()) {
            return entityCuCreator.getPrimaryTypeName();
        } else {
            return entityCuCreator.getPrimaryTypeQualifier();
        }
    }

}