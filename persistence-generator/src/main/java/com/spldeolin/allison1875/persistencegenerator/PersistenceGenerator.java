package com.spldeolin.allison1875.persistencegenerator;

import java.io.File;
import java.util.List;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.persistencegenerator.dto.DetectOrGenerateMapperRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateJoinChainArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.KeyMethodNameDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByKeysDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.DesignGeneratorService;
import com.spldeolin.allison1875.persistencegenerator.service.EntityGeneratorService;
import com.spldeolin.allison1875.persistencegenerator.service.MapperCoidService;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlService;
import com.spldeolin.allison1875.persistencegenerator.service.TableAnalyzerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-11
 */
@Singleton
@Slf4j
public class PersistenceGenerator implements Allison1875MainService {

    @Inject
    private MapperCoidService mapperCoidService;

    @Inject
    private TableAnalyzerService tableAnalyzerService;

    @Inject
    private EntityGeneratorService entityGeneratorService;

    @Inject
    private MapperXmlService mapperXmlService;

    @Inject
    private DesignGeneratorService designGeneratorService;

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void process(AstForest astForest) {
        // 分析表结构
        List<TableAnalysisDTO> tableAnalyses = tableAnalyzerService.analyzeTable();
        if (CollectionUtils.isEmpty(tableAnalyses)) {
            log.warn("no tables detected in Schema [{}] at Connection [{}].", config.getSchema(), config.getJdbcUrl());
            return;
        }

        List<FileFlush> flushes = Lists.newArrayList();
        Mutable<CompilationUnit> joinChainCu = new MutableObject<>();
        for (TableAnalysisDTO tableAnalysis : tableAnalyses) {
            flushes.addAll(tableAnalysis.getFlushes());

            // 生成Entity
            DataModelGeneration entityGeneration = entityGeneratorService.generateEntity(tableAnalysis);
            flushes.add(entityGeneration.getFileFlush());

            // 寻找或创建Mapper
            DetectOrGenerateMapperRetval detectOrGenerateMapperRetval = mapperCoidService.detectOrGenerateMapper(
                    tableAnalysis, entityGeneration);
            ClassOrInterfaceDeclaration mapper = detectOrGenerateMapperRetval.getMapper();

            // 重新生成Design
            GenerateDesignArgs gdArgs = new GenerateDesignArgs();
            gdArgs.setTableAnalysis(tableAnalysis);
            gdArgs.setEntityGeneration(entityGeneration);
            gdArgs.setMapper(mapper);
            GenerateDesignRetval gdRetval = designGeneratorService.generateDesign(gdArgs);
            if (gdRetval.getDesignFile() != null) {
                flushes.add(gdRetval.getDesignFile());
            }

            // 生成JoinChain
            GenerateJoinChainArgs gjcArgs = new GenerateJoinChainArgs();
            gjcArgs.setTableAnalysis(tableAnalysis);
            gjcArgs.setEntityGeneration(entityGeneration);
            gjcArgs.setJoinChainCu(joinChainCu.getValue());
            gjcArgs.setDesignQualifier(gdRetval.getDesignQualifer());
            designGeneratorService.generateJoinChain(gjcArgs).ifPresent(joinChainCu::setValue);

            // 在Mapper中生成基础方法
            GenerateMethodToMapperArgs gmtmArgs = new GenerateMethodToMapperArgs();
            gmtmArgs.setTableAnalysisDTO(tableAnalysis);
            gmtmArgs.setEntityGeneration(entityGeneration);
            gmtmArgs.setMapper(mapper);
            String insertMethodName = mapperCoidService.generateInsertMethodToMapper(gmtmArgs);
            String batchInsertMethodName = mapperCoidService.generateBatchInsertMethodToMapper(gmtmArgs);
            String batchInsertEvenNullMethodName = mapperCoidService.generateBatchInsertEvenNullMethodToMapper(
                    gmtmArgs);
            String batchUpdateMethodName = mapperCoidService.generateBatchUpdateMethodToMapper(gmtmArgs);
            String batchUpdateEvenNullMethodName = mapperCoidService.generateBatchUpdateEvenNullMethodToMapper(
                    gmtmArgs);
            String queryByIdMethodName = mapperCoidService.generateQueryByIdMethodToMapper(gmtmArgs);
            String updateByIdMethodName = mapperCoidService.generateUpdateByIdMethodToMapper(gmtmArgs);
            String updateByIdEvenNullMethodName = mapperCoidService.generateUpdateByIdEvenNullMethodToMapper(gmtmArgs);
            String queryByIdsProcMethodName = mapperCoidService.generateQueryByIdsMethodToMapper(gmtmArgs);
            String queryByIdsEachIdMethodName = mapperCoidService.generateQueryByIdsEachIdMethodToMapper(gmtmArgs);
            List<KeyMethodNameDTO> queryByKeyDTOs = Lists.newArrayList();
            List<KeyMethodNameDTO> deleteByKeyDTOs = Lists.newArrayList();
            List<QueryByKeysDTO> queryByKeysDTOs = Lists.newArrayList();
            for (PropertyDTO key : tableAnalysis.getKeyProperties()) {
                queryByKeyDTOs.add(new KeyMethodNameDTO().setKey(key)
                        .setMethodName(mapperCoidService.generateQueryByKeyMethodToMapper(gmtmArgs.setKey(key))));
                deleteByKeyDTOs.add(new KeyMethodNameDTO().setKey(key)
                        .setMethodName(mapperCoidService.generateDeleteByKeyMethodToMapper(gmtmArgs.setKey(key))));
                queryByKeysDTOs.add(mapperCoidService.generateQueryByKeysMethodToMapper(gmtmArgs.setKey(key)));
            }
            String queryByEntityMethodName = mapperCoidService.generateQueryByEntityMethodToMapper(gmtmArgs);
            String listAllMethodName = mapperCoidService.generateListAllMethodToMapper(gmtmArgs);
            String insertOrUpdateMethodName = mapperCoidService.generateInsertOrUpdateMethodToMapper(gmtmArgs);

            // 将临时删除的开发者自定义方法添加到Mapper的最后
            detectOrGenerateMapperRetval.getCustomMethods().forEach(one -> mapper.getMembers().addLast(one));

            CompilationUnit mapperCu = detectOrGenerateMapperRetval.getMapperCu();
            importExprService.extractQualifiedTypeToImport(mapperCu);
            flushes.add(FileFlush.build(mapperCu));

            // 生成MapperXml的基础方法
            String entityName = getEntityNameInXml(entityGeneration);
            List<List<String>> generateMapperXmlCodes = Lists.newArrayList(
                    mapperXmlService.generateResultMap(tableAnalysis, entityName),
                    mapperXmlService.generateAllCloumnSql(tableAnalysis),
                    mapperXmlService.generateInsertMethod(tableAnalysis, entityName, insertMethodName),
                    mapperXmlService.generateBatchInsertMethod(tableAnalysis, batchInsertMethodName),
                    mapperXmlService.generateBatchInsertEvenNullMethod(tableAnalysis, batchInsertEvenNullMethodName),
                    mapperXmlService.generateBatchUpdateMethod(tableAnalysis, batchUpdateMethodName),
                    mapperXmlService.generateBatchUpdateEvenNullMethod(tableAnalysis, batchUpdateEvenNullMethodName),
                    mapperXmlService.generateQueryByIdMethod(tableAnalysis, queryByIdMethodName),
                    mapperXmlService.generateUpdateByIdMethod(tableAnalysis, entityName, updateByIdMethodName),
                    mapperXmlService.generateUpdateByIdEvenNullMethod(tableAnalysis, entityName,
                            updateByIdEvenNullMethodName),
                    mapperXmlService.generateQueryByIdsMethod(tableAnalysis, queryByIdsProcMethodName),
                    mapperXmlService.generateQueryByIdsMethod(tableAnalysis, queryByIdsEachIdMethodName),
                    mapperXmlService.generateQueryByKeyMethod(tableAnalysis, queryByKeyDTOs),
                    mapperXmlService.generateDeleteByKeyMethod(tableAnalysis, deleteByKeyDTOs),
                    mapperXmlService.generateQueryByKeysMethod(tableAnalysis, queryByKeysDTOs),
                    mapperXmlService.generateQueryByEntityMethod(tableAnalysis, entityName, queryByEntityMethodName),
                    mapperXmlService.generateListAllMethod(tableAnalysis, listAllMethodName),
                    mapperXmlService.generateInsertOrUpdateMethod(tableAnalysis, entityName, insertOrUpdateMethodName));

            // 基础方法替换到MapperXml中
            for (File mapperXmlDirectory : commonConfig.getMapperXmlDirs()) {
                if (!mapperXmlDirectory.exists()) {
                    log.debug("mapperXmlDirectory.mkdirs()={}", mapperXmlDirectory.mkdirs());
                }
                ReplaceMapperXmlMethodsArgs rmxmmArgs = new ReplaceMapperXmlMethodsArgs();
                rmxmmArgs.setTableAnalysis(tableAnalysis);
                rmxmmArgs.setMapper(mapper);
                rmxmmArgs.setMapperXmlDirectory(mapperXmlDirectory.toPath());
                rmxmmArgs.setSourceCodes(generateMapperXmlCodes);
                try {
                    FileFlush xmlFlush = mapperXmlService.replaceMapperXmlMethods(rmxmmArgs);
                    flushes.add(xmlFlush);
                } catch (Exception e) {
                    log.error("fail to replaceMapperXmlMethods, rmxmmArgs={}", rmxmmArgs, e);
                    throw e;
                }
            }
        }

        if (joinChainCu.getValue() != null) {
            flushes.add(FileFlush.build(joinChainCu.getValue()));
        }

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        }
    }

    protected String getEntityNameInXml(DataModelGeneration dataModelGeneration) {
        return dataModelGeneration.getDtoQualifier();
    }

}