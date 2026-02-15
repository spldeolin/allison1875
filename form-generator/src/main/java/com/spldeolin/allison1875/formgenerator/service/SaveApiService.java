package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.SaveApiServiceImpl;

/**
 * @author Deolin 2026-02-15
 */
@ImplementedBy(SaveApiServiceImpl.class)
public interface SaveApiService {

    BlockStmt generateMethodBody(FormDef form);

}
