package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.CreateApiServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(CreateApiServiceImpl.class)
public interface CreateApiService {

    InitializerDeclaration generateCreateInitDec(FormDef formDef);

    BlockStmt generateCreateMethodBody(FormDef form);

}
