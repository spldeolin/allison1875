package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.DeleteApiServiceImpl;

/**
 * @author Deolin 2026-02-15
 */
@ImplementedBy(DeleteApiServiceImpl.class)
public interface DeleteApiService {

    InitializerDeclaration generateDeleteInitDec(FormDef formDef);

    BlockStmt generateMethodBody(FormDef form);

}
