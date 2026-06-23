package com.spldeolin.allison1875.appgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    private final String namespace;

    public AppGeneratorMutationExpansionServiceImpl(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public List<String> expandCreateMethodBody(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setCreatedBy(CurrentUser.getUsername());", form.getVarName()));
        body.addStatement(parseStatement("%s.setUpdatedBy(CurrentUser.getUsername());", form.getVarName()));
        return List.of(namespace + ".common.CurrentUser");
    }

    @Override
    public List<String> expandUpdateMethodBody(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setUpdatedBy(CurrentUser.getUsername());", form.getVarName()));
        return List.of(namespace + ".common.CurrentUser");
    }

    @Override
    public List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
        body.addStatement(parseStatement("dto.setCreatedBy(%s.getCreatedBy());", entityVarName));
        body.addStatement(parseStatement("dto.setUpdatedBy(%s.getUpdatedBy());", entityVarName));
        return Collections.emptyList();
    }

}
