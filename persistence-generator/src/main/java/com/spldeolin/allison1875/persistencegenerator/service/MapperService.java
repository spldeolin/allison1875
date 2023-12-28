package com.spldeolin.allison1875.persistencegenerator.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperServiceImpl.class)
public interface MapperService {

    String batchInsertEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String batchInsert(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String batchUpdateEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String batchUpdate(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String deleteByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper);

    String insertOrUpdate(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String insert(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String listAll(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String queryByEntity(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String queryById(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String queryByIdsEachId(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String queryByIds(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String queryByKey(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper);

    QueryByKeysDto queryByKeys(PersistenceDto persistence, PropertyDto key, ClassOrInterfaceDeclaration mapper);

    String updateByIdEvenNull(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

    String updateById(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper);

}