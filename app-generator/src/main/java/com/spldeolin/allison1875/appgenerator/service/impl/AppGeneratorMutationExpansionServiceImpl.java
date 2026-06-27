package com.spldeolin.allison1875.appgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    private final String namespace;

    private final boolean auditLogEnabled;

    public AppGeneratorMutationExpansionServiceImpl(String namespace, boolean auditLogEnabled) {
        this.namespace = namespace;
        this.auditLogEnabled = auditLogEnabled;
    }

    @Override
    public void expandCreateMethodBody(FormDef form, BlockStmt body) {
        String currentUserFqn = namespace + ".common.CurrentUser";
        body.addStatement(parseStatement("%s.setCreatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));
        body.addStatement(parseStatement("%s.setUpdatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));

        if (auditLogEnabled && !"AuditLog".equals(form.getName())) {
            String enumFqn = namespace + ".enums.AuditOperationTypeEnum";
            String upperSnake = MoreStringUtils.camelToSnakeCase(form.getName()).toUpperCase();
            body.addStatement(parseStatement(
                    "java.util.Map<String, Object> auditContent = new java.util.LinkedHashMap<>();"));
            body.addStatement(parseStatement("auditContent.put(\"%sCode\", %s.%s());",
                    form.getName(), form.getVarName(), form.getBizIdGetterName()));
            for (ItemDef item : form.getNonAuditedItems()) {
                if (item.getType() == ItemType.SECRET || item.getType() == ItemType.MULTI_SELECT) {
                    continue;
                }
                if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                    body.addStatement(parseStatement("auditContent.put(\"%s\", req.get%s());",
                            item.getTitle(), StringUtils.capitalize(item.getName())));
                }
            }
            body.addStatement(parseStatement(
                    "%s auditOperationType = %s.CREATE_%s;", enumFqn, enumFqn, upperSnake));
        }
    }

    @Override
    public void expandUpdateMethodBody(FormDef form, BlockStmt body) {
        String currentUserFqn = namespace + ".common.CurrentUser";
        body.addStatement(parseStatement("%s.setUpdatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));

        if (auditLogEnabled && !"AuditLog".equals(form.getName())) {
            String enumFqn = namespace + ".enums.AuditOperationTypeEnum";
            String upperSnake = MoreStringUtils.camelToSnakeCase(form.getName()).toUpperCase();
            body.addStatement(parseStatement(
                    "java.util.Map<String, Object> oldValues = new java.util.LinkedHashMap<>();"));
            body.addStatement(parseStatement(
                    "java.util.Map<String, Object> newValues = new java.util.LinkedHashMap<>();"));
            for (ItemDef item : form.getNonAuditedItems()) {
                if (item.getType() == ItemType.SECRET || item.getType() == ItemType.MULTI_SELECT) {
                    continue;
                }
                if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                    body.addStatement(parseStatement("oldValues.put(\"%s\", %s.get%s());",
                            item.getTitle(), form.getVarName(), StringUtils.capitalize(item.getName())));
                    body.addStatement(parseStatement("newValues.put(\"%s\", req.get%s());",
                            item.getTitle(), StringUtils.capitalize(item.getName())));
                }
            }
            body.addStatement(parseStatement(
                    "%s auditOperationType = %s.UPDATE_%s;", enumFqn, enumFqn, upperSnake));
        }
    }

    @Override
    public void expandDeleteMethodBody(FormDef form, BlockStmt body) {
        if (auditLogEnabled && !"AuditLog".equals(form.getName())) {
            String enumFqn = namespace + ".enums.AuditOperationTypeEnum";
            String facadeFqn = namespace + ".service.AuditLogFacade";
            String upperSnake = MoreStringUtils.camelToSnakeCase(form.getName()).toUpperCase();
            body.addStatement(parseStatement(
                    "java.util.Map<String, Object> auditContent = new java.util.LinkedHashMap<>();"));
            body.addStatement(parseStatement("auditContent.put(\"%sCode\", req.get%ss());",
                    form.getName(), StringUtils.capitalize(form.getBizIdName())));
            body.addStatement(parseStatement(
                    "auditLogFacade.logSuccess(%s.DELETE_%s, auditContent);", enumFqn, upperSnake));
        }
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
