package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dto.FormDefDTO;
import com.spldeolin.allison1875.formgenerator.service.impl.InitDecServiceImpl;

/**
 * @author Deolin 2025-08-14
 */
@ImplementedBy(InitDecServiceImpl.class)
public interface InitDecService {

    InitializerDeclaration buildCreateHandler(FormDefDTO formDef);

    InitializerDeclaration buildListHandler(FormDefDTO formDef);

    InitializerDeclaration buildGetDetailHandler(FormDefDTO formDef);

    InitializerDeclaration buildUpdateHandler(FormDefDTO formDef);

    InitializerDeclaration buildDeleteHandler(FormDefDTO formDef);

}
