package com.spldeolin.allison1875.formgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.FormGeneratorConfig;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class SaveApiServiceImpl implements SaveApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Inject
    private FormGeneratorConfig formGeneratorConfig;

    @Override
    public InitializerDeclaration generateSaveInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        // handler, desc声明部分
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"save%s\", desc = \"创建%s\", form=\"%s\", type=\"%s\";",
                        form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                        ApiType.SAVE.getCode())));

        // req声明
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = StaticJavaParser.parseBodyDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;").asFieldDeclaration();
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            if (item.getInitPattern() == InitOrEditPattern.USER_INPUT
                    || item.getEditPattern() == InitOrEditPattern.USER_INPUT) {
                FieldDeclaration itemField = StaticJavaParser.parseBodyDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";").asFieldDeclaration();
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp声明
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp").addMember(bizIdField);
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("boolean toCreate = req.%s() == null;", form.getBizIdGetterName())));
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s %s;", form.getEntityName(persistenceGeneratorConfig), form.getVarName())));
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(new NameExpr("toCreate"));
        ifStmt.setThenStmt(generateIfThenBody(form));
        ifStmt.setElseStmt(generateElseBody(form));
        body.addStatement(ifStmt);

        // initPattern==userInput且 editPattern==userInput添加此处

        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.setUpdateTime(LocalDateTime.now());", form.getVarName())));

        body.addStatement(StaticJavaParser.parseStatement(
                "return new Save" + form.getName() + "Resp()." + form.getBizIdSetterName() + "(" + form.getVarName()
                        + "." + form.getBizIdGetterName() + "());"));
        return body;
    }

    private Statement generateIfThenBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s = new %s();", form.getVarName(), form.getEntityName(persistenceGeneratorConfig))));
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.%s(%s);", form.getVarName(), form.getBizIdSetterName(),
                        formGeneratorConfig.getShortUuidGeneration())));
        // initPattern!=userInput添加此处
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.setCreatedAt(LocalDateTime.now());", form.getVarName())));
        return body;
    }

    private Statement generateElseBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s = %sMapper.queryBy%s(req.%s());", form.getVarName(), form.getVarName(),
                        StringUtils.capitalize(form.getBizIdName()), form.getBizIdGetterName())));
        // editPattern!=userInput添加此处
        return body;
    }

}
