package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.impl.AppendAutowiredMapperServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(AppendAutowiredMapperServiceImpl.class)
public interface AppendAutowiredMapperService {

    void append(MethodCallExpr chain, DesignMeta designMeta);

}