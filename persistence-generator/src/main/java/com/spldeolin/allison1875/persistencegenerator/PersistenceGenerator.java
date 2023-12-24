package com.spldeolin.allison1875.persistencegenerator;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.processor.BuildPersistenceDtoService;
import com.spldeolin.allison1875.persistencegenerator.processor.DeleteAllison1875MethodService;
import com.spldeolin.allison1875.persistencegenerator.processor.FindOrCreateMapperService;
import com.spldeolin.allison1875.persistencegenerator.processor.GenerateDesignService;
import com.spldeolin.allison1875.persistencegenerator.processor.GenerateEntityService;
import com.spldeolin.allison1875.persistencegenerator.processor.MapperService;
import com.spldeolin.allison1875.persistencegenerator.processor.MapperXmlFileService;
import com.spldeolin.allison1875.persistencegenerator.processor.MapperXmlService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Singleton
@Log4j2
public class PersistenceGenerator implements Allison1875MainService {

    @Inject
    private MapperService mapperService;

    @Inject
    private BuildPersistenceDtoService buildPersistenceDtoService;

    @Inject
    private DeleteAllison1875MethodService deleteAllison1875MethodService;

    @Inject
    private FindOrCreateMapperService findOrCreateMapperService;

    @Inject
    private GenerateEntityService generateEntityService;

    @Inject
    private MapperXmlService mapperXmlService;

    @Inject
    private MapperXmlFileService mapperXmlFileService;

    @Inject
    private GenerateDesignService generateDesignService;

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public void process(AstForest astForest) {
        // 构建并遍历 PersistenceDto对象
        Collection<PersistenceDto> persistenceDtos = buildPersistenceDtoService.process(astForest);
        if (persistenceDtos.size() == 0) {
            log.warn("no tables detected in Schema [{}] at Connection [{}].", config.getSchema(), config.getJdbcUrl());
            return;
        }

        List<FileFlush> flushes = Lists.newArrayList();
        for (PersistenceDto persistence : persistenceDtos) {

            // 重新生成Entity
            EntityGeneration entityGeneration = generateEntityService.process(persistence, astForest);
            if (entityGeneration.isSameNameAndLotNoPresent()) {
                continue;
            }
            flushes.add(FileFlush.build(entityGeneration.getEntityCu()));

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                mapper = findOrCreateMapperService.process(persistence, entityGeneration, astForest);
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 重新生成Design
            CompilationUnit designCu = generateDesignService.process(persistence, entityGeneration, mapper, astForest);
            if (designCu != null) {
                flushes.add(FileFlush.build(designCu));
            }

            // 删除Mapper中所有Allison 1875生成的并且声明了不可人为修改的方法
            deleteAllison1875MethodService.process(mapper);

            // 临时删除Mapper中所有开发者自定义方法
            List<MethodDeclaration> customMethods = mapper.getMethods();
            customMethods.forEach(MethodDeclaration::remove);

            // 在Mapper中生成基础方法
            String insertMethodName = mapperService.insert(persistence, mapper);
            String batchInsertMethodName = mapperService.batchInsert(persistence, mapper);
            String batchInsertEvenNullMethodName = mapperService.batchInsertEvenNull(persistence, mapper);
            String batchUpdateMethodName = mapperService.batchUpdate(persistence, mapper);
            String batchUpdateEvenNullMethodName = mapperService.batchUpdateEvenNull(persistence, mapper);
            String queryByIdMethodName = mapperService.queryById(persistence, mapper);
            String updateByIdMethodName = mapperService.updateById(persistence, mapper);
            String updateByIdEvenNullMethodName = mapperService.updateByIdEvenNull(persistence, mapper);
            String queryByIdsProcMethodName = mapperService.queryByIds(persistence, mapper);
            String queryByIdsEachIdMethodName = mapperService.queryByIdsEachId(persistence, mapper);
            Collection<KeyMethodNameDto> queryByKeyDtos = Lists.newArrayList();
            Collection<KeyMethodNameDto> deleteByKeyDtos = Lists.newArrayList();
            Collection<QueryByKeysDto> queryByKeysDtos = Lists.newArrayList();
            for (PropertyDto key : persistence.getKeyProperties()) {
                queryByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(mapperService.queryByKey(persistence, key, mapper)));
                deleteByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(mapperService.deleteByKey(persistence, key, mapper)));
                queryByKeysDtos.add(mapperService.queryByKeys(persistence, key, mapper));
            }
            String queryByEntityMethodName = mapperService.queryByEntity(persistence, mapper);
            String listAllMethodName = mapperService.listAll(persistence, mapper);
            String insertOrUpdateMethodName = mapperService.insertOrUpdate(persistence, mapper);

            // 将临时删除的开发者自定义方法添加到Mapper的最后
            customMethods.forEach(one -> mapper.getMembers().addLast(one));

            // 在Mapper.xml中覆盖生成基础方法
            String entityName = getEntityNameInXml(entityGeneration);
            for (String mapperXmlDirectoryPath : config.getMapperXmlDirectoryPaths()) {
                try {
                    Path mapperXmlDirectory = astForest.getAstForestRoot().resolve(mapperXmlDirectoryPath);
                    FileFlush xmlFlush = mapperXmlFileService.process(persistence, mapper, mapperXmlDirectory,
                            Lists.newArrayList(mapperXmlService.resultMapXml(persistence, entityName),
                                    mapperXmlService.allCloumnSqlXml(persistence),
                                    mapperXmlService.insertXml(persistence, entityName, insertMethodName),
                                    mapperXmlService.batchInsertXml(persistence, batchInsertMethodName),
                                    mapperXmlService.batchInsertEvenNullXml(persistence, batchInsertEvenNullMethodName),
                                    mapperXmlService.batchUpdateXml(persistence, batchUpdateMethodName),
                                    mapperXmlService.batchUpdateEvenNullXml(persistence, batchUpdateEvenNullMethodName),
                                    mapperXmlService.queryByIdXml(persistence, queryByIdMethodName),
                                    mapperXmlService.updateByIdXml(persistence, entityName, updateByIdMethodName),
                                    mapperXmlService.updateByIdEvenNullXml(persistence, entityName,
                                            updateByIdEvenNullMethodName),
                                    mapperXmlService.queryByIdsXml(persistence, queryByIdsProcMethodName),
                                    mapperXmlService.queryByIdsXml(persistence, queryByIdsEachIdMethodName),
                                    mapperXmlService.queryByKeyXml(persistence, queryByKeyDtos),
                                    mapperXmlService.deleteByKeyXml(persistence, deleteByKeyDtos),
                                    mapperXmlService.queryByKeysXml(persistence, queryByKeysDtos),
                                    mapperXmlService.queryByEntityXml(persistence, entityName, queryByEntityMethodName),
                                    mapperXmlService.listAllXml(persistence, listAllMethodName),
                                    mapperXmlService.insertOrUpdateXml(persistence, entityName,
                                            insertOrUpdateMethodName)));
                    flushes.add(xmlFlush);
                    mapper.findCompilationUnit().ifPresent(cu -> flushes.add(FileFlush.build(cu)));
                } catch (Exception e) {
                    log.error("写入Mapper.xml时发生异常 persistence={}", persistence, e);
                }
            }
        }

        // write all to file
        if (flushes.size() > 0) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private String getEntityNameInXml(EntityGeneration entityGeneration) {
        if (config.getIsEntityUsingAlias()) {
            return entityGeneration.getEntityName();
        } else {
            return entityGeneration.getEntityQualifier();
        }
    }

}