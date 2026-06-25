package com.spldeolin.allison1875.appgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.common.util.JavadocUtils;
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
    public void expandCreateMethodBody(FormDef form, BlockStmt body) {
        String currentUserFqn = namespace + ".common.CurrentUser";
        body.addStatement(parseStatement("%s.setCreatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));
        body.addStatement(parseStatement("%s.setUpdatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));
    }

    @Override
    public void expandUpdateMethodBody(FormDef form, BlockStmt body) {
        String currentUserFqn = namespace + ".common.CurrentUser";
        body.addStatement(parseStatement("%s.setUpdatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));
    }

    @Override
    public void expandListReqFields(FormDef form, ClassOrInterfaceDeclaration reqCoid) {
        FieldDeclaration createdByField = parseFieldDeclaration("String createdBy;");
        JavadocUtils.setJavadoc(createdByField, "创建人", null);
        reqCoid.addMember(createdByField);

        FieldDeclaration updatedByField = parseFieldDeclaration("String updatedBy;");
        JavadocUtils.setJavadoc(updatedByField, "最近更新人", null);
        reqCoid.addMember(updatedByField);
    }

    @Override
    public void expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
    }

}
