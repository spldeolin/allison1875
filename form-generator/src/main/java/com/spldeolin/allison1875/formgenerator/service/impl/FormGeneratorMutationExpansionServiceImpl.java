package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class FormGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    @Override
    public List<String> expandCreateMethodBody(FormDef form, BlockStmt body) {
        return Collections.emptyList();
    }

    @Override
    public List<String> expandUpdateMethodBody(FormDef form, BlockStmt body) {
        return Collections.emptyList();
    }

    @Override
    public List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
        return Collections.emptyList();
    }

}
