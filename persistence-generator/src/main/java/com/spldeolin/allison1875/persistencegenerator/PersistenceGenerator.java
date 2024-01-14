package com.spldeolin.allison1875.persistencegenerator;

import java.nio.file.Path;
import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.BuildPersistenceDtoService;
import com.spldeolin.allison1875.persistencegenerator.service.DeleteAllison1875MethodService;
import com.spldeolin.allison1875.persistencegenerator.service.FindOrCreateMapperService;
import com.spldeolin.allison1875.persistencegenerator.service.GenerateDesignService;
import com.spldeolin.allison1875.persistencegenerator.service.GenerateEntityService;
import com.spldeolin.allison1875.persistencegenerator.service.MapperService;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlFileService;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlService;
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
        List<PersistenceDto> persistenceDtos = buildPersistenceDtoService.build(astForest);
        if (persistenceDtos.size() == 0) {
            log.warn("no tables detected in Schema [{}] at Connection [{}].", config.getSchema(), config.getJdbcUrl());
            return;
        }

        List<FileFlush> flushes = Lists.newArrayList();
        for (PersistenceDto persistence : persistenceDtos) {

            // 生成Entity
            JavabeanGeneration javabeanGeneration = generateEntityService.generate(persistence, astForest);
            flushes.add(javabeanGeneration.getFileFlush());

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                mapper = findOrCreateMapperService.findOrCreate(persistence, javabeanGeneration, astForest);
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 重新生成Design
            CompilationUnit designCu = generateDesignService.generate(persistence, javabeanGeneration, mapper,
                    astForest);
            if (designCu != null) {
                flushes.add(FileFlush.build(designCu));
            }

            // 删除Mapper中所有声明了LotNoAnnounce或者NoModifyAnnounce的方法
            deleteAllison1875MethodService.deleteMethod(mapper);

            // 临时删除Mapper中所有开发者自定义方法
            List<MethodDeclaration> customMethods = mapper.getMethods();
            customMethods.forEach(MethodDeclaration::remove);

            // 在Mapper中生成基础方法
            String insertMethodName = mapperService.insert(persistence, javabeanGeneration, mapper);
            String batchInsertMethodName = mapperService.batchInsert(persistence, javabeanGeneration, mapper);
            String batchInsertEvenNullMethodName = mapperService.batchInsertEvenNull(persistence, javabeanGeneration,
                    mapper);
            String batchUpdateMethodName = mapperService.batchUpdate(persistence, javabeanGeneration, mapper);
            String batchUpdateEvenNullMethodName = mapperService.batchUpdateEvenNull(persistence, javabeanGeneration,
                    mapper);
            String queryByIdMethodName = mapperService.queryById(persistence, javabeanGeneration, mapper);
            String updateByIdMethodName = mapperService.updateById(persistence, javabeanGeneration, mapper);
            String updateByIdEvenNullMethodName = mapperService.updateByIdEvenNull(persistence, javabeanGeneration,
                    mapper);
            String queryByIdsProcMethodName = mapperService.queryByIds(persistence, javabeanGeneration, mapper);
            String queryByIdsEachIdMethodName = mapperService.queryByIdsEachId(persistence, javabeanGeneration, mapper);
            List<KeyMethodNameDto> queryByKeyDtos = Lists.newArrayList();
            List<KeyMethodNameDto> deleteByKeyDtos = Lists.newArrayList();
            List<QueryByKeysDto> queryByKeysDtos = Lists.newArrayList();
            for (PropertyDto key : persistence.getKeyProperties()) {
                queryByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(mapperService.queryByKey(persistence, javabeanGeneration, key, mapper)));
                deleteByKeyDtos.add(new KeyMethodNameDto().setKey(key)
                        .setMethodName(mapperService.deleteByKey(persistence, key, mapper)));
                queryByKeysDtos.add(mapperService.queryByKeys(persistence, javabeanGeneration, key, mapper));
            }
            String queryByEntityMethodName = mapperService.queryByEntity(persistence, javabeanGeneration, mapper);
            String listAllMethodName = mapperService.listAll(persistence, javabeanGeneration, mapper);
            String insertOrUpdateMethodName = mapperService.insertOrUpdate(persistence, javabeanGeneration, mapper);

            // 将临时删除的开发者自定义方法添加到Mapper的最后
            customMethods.forEach(one -> mapper.getMembers().addLast(one));

            // 在Mapper.xml中覆盖生成基础方法
            String entityName = getEntityNameInXml(javabeanGeneration);
            for (String mapperXmlDirectoryPath : config.getMapperXmlDirectoryPaths()) {
                try {
                    Path mapperXmlDirectory = astForest.getAstForestRoot().resolve(mapperXmlDirectoryPath);
                    FileFlush xmlFlush = mapperXmlFileService.generateMapperXml(persistence, mapper, mapperXmlDirectory,
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

    private String getEntityNameInXml(JavabeanGeneration javabeanGeneration) {
        if (config.getIsEntityUsingAlias()) {
            return javabeanGeneration.getJavabeanName();
        } else {
            return javabeanGeneration.getJavabeanQualifier();
        }
    }

}