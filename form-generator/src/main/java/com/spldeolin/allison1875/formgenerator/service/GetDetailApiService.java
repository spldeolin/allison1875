package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.GetDetailApiServiceImpl;

/**
 * @author Deolin 2026-02-15
 */
@ImplementedBy(GetDetailApiServiceImpl.class)
public interface GetDetailApiService {

    BlockStmt generateMethodBody(FormDef form);

}
