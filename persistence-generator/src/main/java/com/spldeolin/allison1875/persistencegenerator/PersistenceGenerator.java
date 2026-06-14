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
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.persistencegenerator.dto.DeleteByIndexMethodDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.DetectOrGenerateMapperRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateDesignRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateJoinChainArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.IndexDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByIndexMethodDTO;
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
public class PersistenceGenerator implements Allison1875Game {

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
    private Config config;

    @Inject
    private ImportExprService importExprService;


    @Override
    public void play() {
        // 分析表结构
        List<TableAnalysisDTO> tableAnalyses = tableAnalyzerService.analyzeTable();
        if (CollectionUtils.isEmpty(tableAnalyses)) {
            log.warn("no tables detected");
            return;
        }

        Mutable<CompilationUnit> joinChainCu = new MutableObject<>();
        for (TableAnalysisDTO tableAnalysis : tableAnalyses) {

            // 生成Entity
            DataModelGeneration entityGeneration = entityGeneratorService.generateEntity(tableAnalysis);

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
            String updateByIdMethodName = mapperCoidService.generateUpdateByIdMethodToMapper(gmtmArgs);
            String updateByIdEvenNullMethodName = mapperCoidService.generateUpdateByIdEvenNullMethodToMapper(gmtmArgs);
            String deleteByIdMethodName = mapperCoidService.generateDeleteByIdMethodToMapper(gmtmArgs);

            String queryByIdMethodName = mapperCoidService.generateQueryByIdMethodToMapper(gmtmArgs);
            String queryByIdsProcMethodName = mapperCoidService.generateQueryByIdsMethodToMapper(gmtmArgs);
            String queryByIdsEachIdMethodName = mapperCoidService.generateQueryByIdsEachIdMethodToMapper(gmtmArgs);

            List<QueryByIndexMethodDTO> queryByIndexMethodNames = Lists.newArrayList();
            List<DeleteByIndexMethodDTO> deleteByIndexMethodNames = Lists.newArrayList();
            List<QueryByIndexMethodDTO> queryByBizIdsMethodNames = Lists.newArrayList();
            List<QueryByIndexMethodDTO> queryByBizIdsEachIdMethodNames = Lists.newArrayList();
            for (IndexDTO index : tableAnalysis.getIndices()) {
                queryByIndexMethodNames.add(
                        mapperCoidService.generateQueryByIndexMethodToMapper(gmtmArgs, index.getProperties(),
                                index.getIsUnique()));
                deleteByIndexMethodNames.add(
                        mapperCoidService.generateDeleteByIndexMethodToMapper(gmtmArgs, index.getProperties()));
                if (index.isBizId()) {
                    PropertyDTO bizId = index.getProperties().get(0);
                    queryByBizIdsMethodNames.add(
                            mapperCoidService.generateQueryByBizIdsMethodToMapper(gmtmArgs, bizId));
                    queryByBizIdsEachIdMethodNames.add(
                            mapperCoidService.generateQueryByBizIdsEachIdMethodToMapper(gmtmArgs, bizId));
                }
            }

            String listAllMethodName = mapperCoidService.generateListAllMethodToMapper(gmtmArgs);

            // 将临时删除的开发者自定义方法添加到Mapper的最后
            detectOrGenerateMapperRetval.getCustomMethods().forEach(one -> mapper.getMembers().addLast(one));

            CompilationUnit mapperCu = detectOrGenerateMapperRetval.getMapperCu();
            importExprService.extractQualifiedTypeToImport(mapperCu);
            CompilationUnitUtils.writeJava(mapperCu);

            // 生成MapperXml的基础方法
            String entityName = getEntityNameInXml(entityGeneration);
            List<List<String>> generateMapperXmlCodes = Lists.newArrayList(
                    mapperXmlService.generateResultMap(tableAnalysis, entityName),
                    mapperXmlService.generateAllCloumnSql(tableAnalysis),
                    mapperXmlService.generateInsertMethod(tableAnalysis, entityName, insertMethodName),
                    mapperXmlService.generateBatchInsertMethod(tableAnalysis, batchInsertMethodName),
                    mapperXmlService.generateBatchInsertEvenNullMethod(tableAnalysis, batchInsertEvenNullMethodName),
                    mapperXmlService.generateUpdateByIdMethod(tableAnalysis, entityName, updateByIdMethodName),
                    mapperXmlService.generateUpdateByIdEvenNullMethod(tableAnalysis, entityName,
                            updateByIdEvenNullMethodName),
                    mapperXmlService.generateDeleteByIdMethod(tableAnalysis, deleteByIdMethodName),

                    mapperXmlService.generateQueryByIdMethod(tableAnalysis, queryByIdMethodName),
                    mapperXmlService.generateQueryByIdsMethod(tableAnalysis, queryByIdsProcMethodName),
                    mapperXmlService.generateQueryByIdsMethod(tableAnalysis, queryByIdsEachIdMethodName),

                    mapperXmlService.generateQueryByIndexMethod(tableAnalysis, queryByIndexMethodNames),
                    mapperXmlService.generateDeleteByIndexMethod(tableAnalysis, deleteByIndexMethodNames),
                    mapperXmlService.generateQueryByBizIdsMethod(tableAnalysis, queryByBizIdsMethodNames),
                    mapperXmlService.generateQueryByBizIdsMethod(tableAnalysis, queryByBizIdsEachIdMethodNames),

                    mapperXmlService.generateListAllMethod(tableAnalysis, listAllMethodName));

            // 基础方法替换到MapperXml中
            for (File mapperXmlDirectory : DomainContext.get().getMapperXmlDirs()) {
                if (!mapperXmlDirectory.exists()) {
                    log.debug("mapperXmlDirectory.mkdirs()={}", mapperXmlDirectory.mkdirs());
                }
                ReplaceMapperXmlMethodsArgs rmxmmArgs = new ReplaceMapperXmlMethodsArgs();
                rmxmmArgs.setTableAnalysis(tableAnalysis);
                rmxmmArgs.setMapper(mapper);
                rmxmmArgs.setMapperXmlDirectory(mapperXmlDirectory.toPath());
                rmxmmArgs.setSourceCodes(generateMapperXmlCodes);
                try {
                    mapperXmlService.replaceMapperXmlMethods(rmxmmArgs);
                } catch (Exception e) {
                    log.error("fail to replaceMapperXmlMethods, rmxmmArgs={}", rmxmmArgs, e);
                    throw e;
                }
            }
        }

        log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
    }

    protected String getEntityNameInXml(DataModelGeneration dataModelGeneration) {
        return dataModelGeneration.getDtoQualifier();
    }

}