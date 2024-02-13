package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.service.impl.AutowiredMapperAdderServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(AutowiredMapperAdderServiceImpl.class)
public interface AutowiredMapperAdderService {

    void addAutowiredMapper(MethodCallExpr chain, DesignMetaDto designMeta);

}