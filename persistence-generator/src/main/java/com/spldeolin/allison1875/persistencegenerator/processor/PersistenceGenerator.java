package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
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
import com.spldeolin.allison1875.persistencegenerator.strategy.DefaultGenerateEntityFieldCallback;
import com.spldeolin.allison1875.persistencegenerator.strategy.DefaultGenerateQueryDesignFieldCallback;
import com.spldeolin.allison1875.persistencegenerator.strategy.GenerateEntityFieldCallback;
import com.spldeolin.allison1875.persistencegenerator.strategy.GenerateQueryDesignFieldCallback;

/**
 * @author Deolin 2020-07-11
 */
public class PersistenceGenerator implements
        Allison1875MainProcessor<PersistenceGeneratorConfig, PersistenceGenerator> {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PersistenceGenerator.class);

    private GenerateEntityFieldCallback generateEntityFieldCallback = new DefaultGenerateEntityFieldCallback();

    private GenerateQueryDesignFieldCallback generateQueryDesignFieldCallback =
            new DefaultGenerateQueryDesignFieldCallback();

    public static final ThreadLocal<PersistenceGeneratorConfig> CONFIG = ThreadLocal
            .withInitial(PersistenceGeneratorConfig::new);

    @Override
    public PersistenceGenerator config(PersistenceGeneratorConfig config) {
        Set<ConstraintViolation<PersistenceGeneratorConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<PersistenceGeneratorConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        CONFIG.set(config);
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);
        PathProc pathProc = new PathProc(astForest).process();

        Collection<CompilationUnit> toSave = Lists.newArrayList();

        // 构建并遍历 PersistenceDto对象
        for (PersistenceDto persistence : new BuildPersistenceDtoProc().process().getPersistences()) {

            // 重新生成Entity
            GenerateEntityProc entityProc = new GenerateEntityProc(generateEntityFieldCallback, persistence, pathProc)
                    .process();
            CuCreator entityCuCreator = entityProc.getEntityCuCreator();
            toSave.addAll(entityProc.getToCreate());

            // 寻找或创建Mapper
            ClassOrInterfaceDeclaration mapper;
            try {
                FindOrCreateMapperProc proc = new FindOrCreateMapperProc(persistence, entityCuCreator).process();
                mapper = proc.getMapper();
                toSave.add(proc.getCu());
            } catch (Exception e) {
                log.error("寻找或创建Mapper时发生异常 persistence={}", persistence, e);
                continue;
            }

            // 重新生成QueryDesign
            toSave.addAll(
                    new GenerateQueryDesignProc(persistence, entityCuCreator, mapper, generateQueryDesignFieldCallback)
                            .process().getToCreate());

            // 删除Mapper中所有Allison 1875生成的方法
            new DeleteAllison1875MethodProc(mapper).process();

            // 在Mapper中生成基础方法
            InsertProc insertProc = new InsertProc(persistence, mapper).process();
            BatchInsertEvenNullProc batchInsertEvenNullProc = new BatchInsertEvenNullProc(persistence, mapper)
                    .process();
            QueryByIdProc queryByIdProc = new QueryByIdProc(persistence, mapper).process();
            UpdateByIdProc updateByIdProc = new UpdateByIdProc(persistence, mapper).process();
            UpdateByIdEvenNullProc updateByIdEvenNullProc = new UpdateByIdEvenNullProc(persistence, mapper).process();
            QueryByIdsProc queryByIdsProc = new QueryByIdsProc(persistence, mapper).process();
            QueryByIdsEachIdProc queryByIdsEachIdProc = new QueryByIdsEachIdProc(persistence, mapper).process();
            Collection<QueryByKeyProc> queryByKeyProcs = Lists.newArrayList();
            Collection<DeleteByKeyProc> deleteByKeyProcs = Lists.newArrayList();
            Collection<QueryByKeysProc> queryByKeysProcs = Lists.newArrayList();
            for (PropertyDto key : persistence.getKeyProperties()) {
                queryByKeyProcs.add(new QueryByKeyProc(persistence, key, mapper).process());
                deleteByKeyProcs.add(new DeleteByKeyProc(persistence, key, mapper).process());
                queryByKeysProcs.add(new QueryByKeysProc(persistence, key, mapper).process());
            }
            QueryByEntityProc queryByEntityProc = new QueryByEntityProc(persistence, mapper).process();

            // 在Mapper.xml中覆盖生成基础方法
            String entityName = getEntityNameInXml(entityCuCreator);
            try {
                new MapperXmlProc(persistence, mapper, pathProc.getMapperXmlPath(),
                        new ResultMapXmlProc(persistence, entityName).process(),
                        new AllCloumnSqlXmlProc(persistence).process(),
                        new InsertXmlProc(persistence, entityName, insertProc).process(),
                        new BatchInsertEvenNullXmlProc(persistence, batchInsertEvenNullProc).process(),
                        new QueryByIdXmlProc(persistence, queryByIdProc).process(),
                        new UpdateByIdXmlProc(persistence, entityName, updateByIdProc).process(),
                        new UpdateByIdEvenNullXmlProc(persistence, entityName, updateByIdEvenNullProc).process(),
                        new QueryByIdsXmlProc(persistence, queryByIdsProc).process(),
                        new QueryByIdsXmlProc(persistence, queryByIdsEachIdProc).process(),
                        new QueryByKeyXmlProc(persistence, queryByKeyProcs).process(),
                        new DeleteByKeyXmlProc(persistence, deleteByKeyProcs).process(),
                        new QueryByKeysXmlProc(persistence, queryByKeysProcs).process(),
                        new QueryByEntityXmlProc(persistence, entityName, queryByEntityProc).process()).process();
            } catch (Exception e) {
                log.error("写入Mapper.xml时发生异常 persistence={}", persistence, e);
            }
        }

        toSave.forEach(Saves::save);
    }

    private static String getEntityNameInXml(CuCreator entityCuCreator) {
        if (PersistenceGenerator.CONFIG.get().getIsEntityUsingAlias()) {
            return entityCuCreator.getPrimaryTypeName();
        } else {
            return entityCuCreator.getPrimaryTypeQualifier();
        }
    }

    public void setGenerateEntityFieldCallback(GenerateEntityFieldCallback generateEntityFieldCallback) {
        this.generateEntityFieldCallback = generateEntityFieldCallback;
    }

    public void setGenerateQueryDesignFieldCallback(GenerateQueryDesignFieldCallback generateQueryDesignFieldCallback) {
        this.generateQueryDesignFieldCallback = generateQueryDesignFieldCallback;
    }

}