package com.spldeolin.allison1875.formgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.InitDecService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2025-08-14
 */
@Singleton
public class InitDecServiceImpl implements InitDecService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public InitializerDeclaration buildSaveHandler(FormDef form) {
        BlockStmt bs = new BlockStmt();
        // handler, desc声明部分
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"create%s\", desc = \"创建%s\", form=\"%s\";",
                        MoreStringUtils.toUpperCamel(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)))));

        // req声明
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = StaticJavaParser.parseBodyDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;").asFieldDeclaration();
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            if (item.getInitPattern() == InitOrEditPattern.USER_INPUT) {
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
    public InitializerDeclaration buildListHandler(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"list%s\", desc = \"%s列表\", form=\"%s\";",
                        MoreStringUtils.toUpperCamel(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)))));

        // req声明
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.SECRET) {
                continue;
            }
            FieldDeclaration itemField = StaticJavaParser.parseBodyDeclaration(
                    itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";").asFieldDeclaration();
            JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
            itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
            reqCoid.addMember(itemField);
        }
        FieldDeclaration pageNum = StaticJavaParser.parseBodyDeclaration("Integer pageNum;").asFieldDeclaration();
        JavadocUtils.setJavadoc(pageNum, "分页页码", null);
        reqCoid.addMember(pageNum);
        FieldDeclaration pageSize = StaticJavaParser.parseBodyDeclaration("Integer pageSize;").asFieldDeclaration();
        JavadocUtils.setJavadoc(pageSize, "分页条数", null);
        reqCoid.addMember(pageSize);
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp声明
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.SECRET) {
                continue;
            }
            FieldDeclaration itemField = StaticJavaParser.parseBodyDeclaration(
                    itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";").asFieldDeclaration();
            JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
            itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
            respCoid.addMember(itemField);
        }
        bs.addStatement(new LocalClassDeclarationStmt(respCoid.addAnnotation("com.spldeolin.allison1875.support.P")));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildGetDetailHandler(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"get%sDetail\", desc = \"%s详情\", form=\"%s\";",
                        MoreStringUtils.toUpperCamel(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)))));

        // req声明
        FieldDeclaration bizIdField = StaticJavaParser.parseBodyDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;").asFieldDeclaration();
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req")
                .addMember(bizIdField.clone().addAnnotation(annotationExprService.notNull()));
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp声明
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.SECRET) {
                continue;
            }
            FieldDeclaration itemField = StaticJavaParser.parseBodyDeclaration(
                    itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";").asFieldDeclaration();
            JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
            itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
            respCoid.addMember(itemField);
        }
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildDeleteHandler(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"delete%s\", desc = \"删除%s\", form=\"%s\";",
                        MoreStringUtils.toUpperCamel(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)))));

        // req声明
        FieldDeclaration bizIdField = StaticJavaParser.parseBodyDeclaration(
                "java.util.List<String> " + StringUtils.uncapitalize(form.getName()) + "Codes;").asFieldDeclaration();
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req")
                .addMember(bizIdField.clone().addAnnotation(annotationExprService.notEmpty()));
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));
        return new InitializerDeclaration(false, bs);
    }

}
