package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.UpdateApiServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(UpdateApiServiceImpl.class)
public interface UpdateApiService {

    InitializerDeclaration generateUpdateInitDec(FormDef formDef);

    BlockStmt generateUpdateMethodBody(FormDef form);

}
