package com.spldeolin.allison1875.pg.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.processor.mapper.DeleteByKeyProc;
import com.spldeolin.allison1875.pg.processor.mapper.InsertProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByKeyProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByIdProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByIdsEachPkProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByIdsProc;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByIdEvenNullProc;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByIdProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.AllCloumnSqlXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.DeleteByKeyXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.InsertXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByKeyXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByIdXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByIdsXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.ResultMapXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.UpdateByIdEvenNullXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.UpdateByIdXmlProc;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Log4j2
public class MainProc {


    /**
     * @see adfa
     */
    public static void main(String[] args) {
        Collection<CompilationUnit> toSave = Lists.newArrayList();

        // 构建并遍历 PersistenceDto对象
        for (PersistenceDto persistence : new BuildPersistenceDtoProc().process().getPersistences()) {

            // 重新生成Entity
            EntityProc entityProc = new EntityProc(persistence).process();
            CuCreator entityCuCreator = entityProc.getEntityCuCreator();
            toSave.add(entityCuCreator.create(false));

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

            // 在Mapper中生成基础方法
            InsertProc insertProc = new InsertProc(persistence, mapper).process();
            QueryByIdProc queryByPkProc = new QueryByIdProc(persistence, mapper).process();
            UpdateByIdProc updateByPkProc = new UpdateByIdProc(persistence, mapper).process();
            UpdateByIdEvenNullProc updateByPkEvenNullProc = new UpdateByIdEvenNullProc(persistence, mapper).process();
            new QueryByIdsEachPkProc(persistence, mapper).process();
            QueryByIdsProc queryByPksProc = new QueryByIdsProc(persistence, mapper).process();
            QueryByKeyProc queryByFkProc = new QueryByKeyProc(persistence, mapper).process();
            DeleteByKeyProc deleteByFkProc = new DeleteByKeyProc(persistence, mapper).process();

            // 在Mapper.xml中生成基础方法
            String entityName = getEntityNameInXml(entityCuCreator);
            try {
                new MapperXmlProc(persistence, mapper, new ResultMapXmlProc(persistence, entityName).process(),
                        new AllCloumnSqlXmlProc(persistence).process(),
                        new InsertXmlProc(persistence, entityName, insertProc).process(),
                        new QueryByIdXmlProc(persistence, queryByPkProc).process(),
                        new UpdateByIdXmlProc(persistence, entityName, updateByPkProc).process(),
                        new UpdateByIdEvenNullXmlProc(persistence, entityName, updateByPkEvenNullProc).process(),
                        new QueryByIdsXmlProc(persistence, "queryByIds", queryByPksProc).process(),
                        new QueryByIdsXmlProc(persistence, "queryByIdsEachId", queryByPksProc).process(),
                        new QueryByKeyXmlProc(persistence, queryByFkProc).process(),
                        new DeleteByKeyXmlProc(persistence, deleteByFkProc).process()).process();
            } catch (Exception e) {
                log.error("写入Mapper.xml时发生异常 persistence={}", persistence, e);
            }

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