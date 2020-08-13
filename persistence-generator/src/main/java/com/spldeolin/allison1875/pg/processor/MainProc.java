package com.spldeolin.allison1875.pg.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.pg.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.pg.javabean.PersistenceDto;
import com.spldeolin.allison1875.pg.processor.mapper.DeleteByFkProc;
import com.spldeolin.allison1875.pg.processor.mapper.InsertProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByFkProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByPkProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByPksEachPkProc;
import com.spldeolin.allison1875.pg.processor.mapper.QueryByPksProc;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByPkEvenNullProc;
import com.spldeolin.allison1875.pg.processor.mapper.UpdateByPkProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.AllCloumnSqlXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.DeleteByFkXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.InsertXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByFkXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByPkXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.QueryByPksXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.ResultMapXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.UpdateByPkEvenNullXmlProc;
import com.spldeolin.allison1875.pg.processor.mapperxml.UpdateByPkXmlProc;
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
            QueryByPkProc queryByPkProc = new QueryByPkProc(persistence, mapper).process();
            UpdateByPkProc updateByPkProc = new UpdateByPkProc(persistence, mapper).process();
            UpdateByPkEvenNullProc updateByPkEvenNullProc = new UpdateByPkEvenNullProc(persistence, mapper).process();
            new QueryByPksEachPkProc(persistence, mapper).process();
            QueryByPksProc queryByPksProc = new QueryByPksProc(persistence, mapper).process();
            QueryByFkProc queryByFkProc = new QueryByFkProc(persistence, mapper).process();
            DeleteByFkProc deleteByFkProc = new DeleteByFkProc(persistence, mapper).process();

            // 在Mapper.xml中生成基础方法
            String entityName = getEntityNameInXml(entityCuCreator);
            try {
                new MapperXmlProc(persistence, mapper, new ResultMapXmlProc(persistence, entityName).process(),
                        new AllCloumnSqlXmlProc(persistence).process(),
                        new InsertXmlProc(persistence, entityName, insertProc).process(),
                        new QueryByPkXmlProc(persistence, queryByPkProc).process(),
                        new UpdateByPkXmlProc(persistence, entityName, updateByPkProc).process(),
                        new UpdateByPkEvenNullXmlProc(persistence, entityName, updateByPkEvenNullProc).process(),
                        new QueryByPksXmlProc(persistence, "queryByIds", queryByPksProc).process(),
                        new QueryByPksXmlProc(persistence, "queryByIdsEachId", queryByPksProc).process(),
                        new QueryByFkXmlProc(persistence, queryByFkProc).process(),
                        new DeleteByFkXmlProc(persistence, deleteByFkProc).process()).process();
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