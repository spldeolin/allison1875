package com.spldeolin.allison1875.persistencegenerator.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperServiceImpl.class)
public interface MapperService {

    String batchInsertEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String batchInsert(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String batchUpdateEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String batchUpdate(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String deleteByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper);

    String insertOrUpdate(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String insert(PersistenceDto persistence, JavabeanGeneration entityGeneration, ClassOrInterfaceDeclaration mapper);

    String listAll(PersistenceDto persistence, JavabeanGeneration entityGeneration, ClassOrInterfaceDeclaration mapper);

    String queryByEntity(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String queryById(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String queryByIdsEachId(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String queryByIds(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String queryByKey(PersistenceDto persistence, JavabeanGeneration entityGeneration, PropertyDto key,
            ClassOrInterfaceDeclaration mapper);

    QueryByKeysDto queryByKeys(PersistenceDto persistence, JavabeanGeneration entityGeneration, PropertyDto key,
            ClassOrInterfaceDeclaration mapper);

    String updateByIdEvenNull(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

    String updateById(PersistenceDto persistence, JavabeanGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper);

}