package com.spldeolin.allison1875.appgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.StaticJavaParser;
import com.google.common.collect.Lists;
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
        }
    }

    @Override
    public void expandUpdateMethodBody(FormDef form, BlockStmt body) {
        String currentUserFqn = namespace + ".common.CurrentUser";
        body.addStatement(parseStatement("%s.setUpdatedBy(%s.getUsername());", form.getVarName(), currentUserFqn));

        if (auditLogEnabled && !"AuditLog".equals(form.getName())) {
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
        }
    }

    @Override
    public void expandDeleteMethodBody(FormDef form, BlockStmt body) {
        if (auditLogEnabled && !"AuditLog".equals(form.getName())) {
            String enumFqn = namespace + ".enums.AuditOperationTypeEnum";
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

    @Override
    public void postProcessMethodBody(FormDef form, BlockStmt body, String apiType) {
        if (!auditLogEnabled || "AuditLog".equals(form.getName())) {
            return;
        }

        String upperSnake = MoreStringUtils.camelToSnakeCase(form.getName()).toUpperCase();
        String bizExceptionFqn = namespace + ".common.BizException";

        if ("create".equals(apiType)) {
            postProcessCreateBody(body, upperSnake, bizExceptionFqn);
        } else if ("update".equals(apiType)) {
            postProcessUpdateBody(body,  upperSnake, bizExceptionFqn);
        }
    }

    @Override
    public List<FieldDeclaration> expandServiceImplFields(FormDef form) {
        if (!auditLogEnabled || "AuditLog".equals(form.getName())) {
            return Collections.emptyList();
        }
        String facadeType = namespace + ".service.AuditLogFacade";
        FieldDeclaration field = parseFieldDeclaration("private %s auditLogFacade;", facadeType);
        field.addAnnotation(new MarkerAnnotationExpr("javax.annotation.Resource"));
        return Lists.newArrayList(field);
    }

    private void postProcessCreateBody(BlockStmt body, String upperSnake, String bizExceptionFqn) {
        // Find the index of the auditContent map declaration
        int auditStartIdx = findStatementIndex(body, "auditContent");
        if (auditStartIdx < 0) {
            return;
        }

        // Extract statements from auditStartIdx to end
        List<Statement> auditedStatements = new ArrayList<>(
                body.getStatements().subList(auditStartIdx, body.getStatements().size()));
        // Remove them from body
        while (body.getStatements().size() > auditStartIdx) {
            body.getStatements().removeLast();
        }

        // Build try block: audited statements + logSuccess before return
        BlockStmt tryBlock = new BlockStmt();
        Statement returnStmt = auditedStatements.remove(auditedStatements.size() - 1);
        auditedStatements.forEach(tryBlock::addStatement);
        tryBlock.addStatement(parseStatement("auditLogFacade.logSuccess(AuditOperationTypeEnum.CREATE_%s, auditContent);",
                 upperSnake));
        tryBlock.addStatement(returnStmt);

        // Build catch block
        BlockStmt catchBlock = new BlockStmt();
        catchBlock.addStatement(parseStatement(
                "auditLogFacade.logFailure(AuditOperationTypeEnum.CREATE_%s, e.getMessage());", upperSnake));
        catchBlock.addStatement(parseStatement("throw e;"));

        // Build try-catch statement
        TryStmt tryStmt = new TryStmt();
        tryStmt.setTryBlock(tryBlock);
        CatchClause catchClause = new CatchClause(
                new Parameter(StaticJavaParser.parseType(bizExceptionFqn), "e"), catchBlock);
        tryStmt.setCatchClauses(new NodeList<>(catchClause));
        body.addStatement(tryStmt);
    }

    private void postProcessUpdateBody(BlockStmt body, String upperSnake, String bizExceptionFqn) {
        // Find the index of the oldValues map declaration
        int auditStartIdx = findStatementIndex(body, "oldValues");
        if (auditStartIdx < 0) {
            return;
        }

        // Extract statements from auditStartIdx to end
        List<Statement> auditedStatements = new ArrayList<>(
                body.getStatements().subList(auditStartIdx, body.getStatements().size()));
        // Remove them from body
        while (body.getStatements().size() > auditStartIdx) {
            body.getStatements().removeLast();
        }

        // Build try block: audited statements + logUpdateSuccess
        BlockStmt tryBlock = new BlockStmt();
        auditedStatements.forEach(tryBlock::addStatement);
        tryBlock.addStatement(parseStatement(
                "auditLogFacade.logUpdateSuccess(AuditOperationTypeEnum.UPDATE_%s, oldValues, newValues);", upperSnake));

        // Build catch block
        BlockStmt catchBlock = new BlockStmt();
        catchBlock.addStatement(parseStatement(
                "auditLogFacade.logUpdateFailure(AuditOperationTypeEnum.UPDATE_%s, e.getMessage());",
                upperSnake));
        catchBlock.addStatement(parseStatement("throw e;"));

        // Build try-catch statement
        TryStmt tryStmt = new TryStmt();
        tryStmt.setTryBlock(tryBlock);
        CatchClause catchClause = new CatchClause(
                new Parameter(StaticJavaParser.parseType(bizExceptionFqn), "e"), catchBlock);
        tryStmt.setCatchClauses(new NodeList<>(catchClause));
        body.addStatement(tryStmt);
    }

    private int findStatementIndex(BlockStmt body, String marker) {
        for (int i = 0; i < body.getStatements().size(); i++) {
            if (body.getStatement(i).toString().contains(marker)) {
                return i;
            }
        }
        return -1;
    }

}
