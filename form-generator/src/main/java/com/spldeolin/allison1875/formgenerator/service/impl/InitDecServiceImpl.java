package com.spldeolin.allison1875.formgenerator.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dto.FormDefDTO;
import com.spldeolin.allison1875.formgenerator.service.InitDecService;

/**
 * @author Deolin 2025-08-14
 */
@Singleton
public class InitDecServiceImpl implements InitDecService {

    @Override
    public InitializerDeclaration buildCreateHandler(FormDefDTO formDef) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"create%s\", desc = \"创建%s\", formDef=\"%s\";",
                        MoreStringUtils.toUpperCamel(formDef.getName()), formDef.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(formDef)))));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildListHandler(FormDefDTO formDef) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"list%s\", desc = \"%s列表\", formDef=\"%s\";",
                        MoreStringUtils.toUpperCamel(formDef.getName()), formDef.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(formDef)))));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildGetDetailHandler(FormDefDTO formDef) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"get%sDetail\", desc = \"%s详情\", formDef=\"%s\";",
                        MoreStringUtils.toUpperCamel(formDef.getName()), formDef.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(formDef)))));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildUpdateHandler(FormDefDTO formDef) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"update%s\", desc = \"更新%s\", formDef=\"%s\";",
                        MoreStringUtils.toUpperCamel(formDef.getName()), formDef.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(formDef)))));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public InitializerDeclaration buildDeleteHandler(FormDefDTO formDef) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"delete%s\", desc = \"删除%s\", formDef=\"%s\";",
                        MoreStringUtils.toUpperCamel(formDef.getName()), formDef.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(formDef)))));
        return new InitializerDeclaration(false, bs);
    }

}
