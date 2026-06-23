package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMutationExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(FormGeneratorMutationExpansionServiceImpl.class)
public interface MutationExpansionService {

    List<String> expandCreateMethodBody(FormDef form, BlockStmt body);

    List<String> expandUpdateMethodBody(FormDef form, BlockStmt body);

    List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName);

}
