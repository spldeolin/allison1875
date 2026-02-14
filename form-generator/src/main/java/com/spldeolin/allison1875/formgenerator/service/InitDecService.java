package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.InitDecServiceImpl;

/**
 * @author Deolin 2025-08-14
 */
@ImplementedBy(InitDecServiceImpl.class)
public interface InitDecService {

    InitializerDeclaration buildCreateHandler(FormDef formDef);

    InitializerDeclaration buildListHandler(FormDef formDef);

    InitializerDeclaration buildGetDetailHandler(FormDef formDef);

    InitializerDeclaration buildUpdateHandler(FormDef formDef);

    InitializerDeclaration buildDeleteHandler(FormDef formDef);

}
