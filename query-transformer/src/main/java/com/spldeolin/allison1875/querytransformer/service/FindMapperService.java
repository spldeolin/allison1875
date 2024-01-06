package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.impl.FindMapperServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(FindMapperServiceImpl.class)
public interface FindMapperService {

    ClassOrInterfaceDeclaration findMapper(AstForest astForest, DesignMeta designMeta);

}