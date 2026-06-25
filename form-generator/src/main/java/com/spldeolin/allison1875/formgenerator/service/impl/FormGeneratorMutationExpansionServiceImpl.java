package com.spldeolin.allison1875.formgenerator.service.impl;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class FormGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    @Override
    public void expandCreateMethodBody(FormDef form, BlockStmt body) {
    }

    @Override
    public void expandUpdateMethodBody(FormDef form, BlockStmt body) {
    }

    @Override
    public void expandListReqFields(FormDef form, ClassOrInterfaceDeclaration reqCoid) {
    }

    @Override
    public void expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
    }

}
