package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Log4j2
public class MainProcessor {


    /**
     * @see adfa
     */
    public static void main(String[] args) {
        Collection<CompilationUnit> toSave = Lists.newArrayList();

        // 构建并遍历 PersistenceDto对象
        for (PersistenceDto persistence : new BuildPersistenceDtoProcessor().process().getPersistences()) {

            // 重新生成Entity
            EntityProcessor entityProcessor = new EntityProcessor(persistence).process();
            CuCreator entityCuCreator = entityProcessor.getEntityCuCreator();
            toSave.add(entityCuCreator.create(false));

            // 寻找或创建Mapper
//            ClassOrInterfaceDeclaration mapper;
//            try {
//                FindOrCreateMapperProcessor processor = new FindOrCreateMapperProcessor(persistence, entityCuCreator)
//                        .process();
//                mapper = processor.getMapper();
//                toSave.add(processor.getCu());
//            } catch (Exception e) {
//                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
//                continue;
//            }
//
//            // 在Mapper中生成基础方法
//            new InsertProcessor(persistence, mapper).process();
//            new QueryByIdsEachIdProcessor(persistence, mapper).process();
//            new UpdateByIdProcessor(persistence, mapper).process();
//            new UpdateByIdEvenNullProcessor(persistence, mapper).process();
//            new QueryByIdProcessor(persistence, mapper).process();
//            new QueryByIdsProcessor(persistence, mapper).process();
//            new QueryByIdsEachIdProcessor(persistence, mapper).process();
//
//            // 寻找或创建Mapper.xml
//            File mapperXmlFile;
//            Element root;
//            try {
//                FindOrCreateMapperXmlProcessor processor = new FindOrCreateMapperXmlProcessor(persistence, mapper)
//                        .process();
//                mapperXmlFile = processor.getMapperXmlFile();
//                root = processor.getRoot();
//            } catch (DocumentException e) {
//                log.error("寻找或创建Mapper.xml时发生异常 persistence={}", persistence, e);
//                continue;
//            }
//
//            // 在Mapper.xml中生成基础方法
//            String entityName = getEntityNameInXml(entityCuCreator);
//            new AllColumnResultMapProcessor(persistence, entityName, root).process();
//            new AllColumnSqlProcessor(persistence, root).process();
//            new InsertXmlProcessor(persistence, entityName, root).process();
//            new UpdateByIdXmlProcessor(persistence, entityName, root).process();
//            new UpdateByIdEvenNullXmlProcessor(persistence, entityName, root).process();
//            new QueryByIdXmlProcessor(persistence, root).process();
//            new QueryByIdsXmlProcessor(persistence, root, "queryByIds").process();
//            new QueryByIdsXmlProcessor(persistence, root, "queryByIdsEachId").process();
//
//            Dom4jUtils.write(mapperXmlFile, root);
        }

        toSave.forEach(Saves::prettySave);
    }

    private static String getEntityNameInXml(CuCreator entityCuCreator) {
        if (PersistenceGeneratorConfig.getInstace().getIsEntityUsingAlias()) {
            return entityCuCreator.getPrimaryTypeName();
        } else {
            return entityCuCreator.getPrimaryTypeQualifier();
        }
    }

}