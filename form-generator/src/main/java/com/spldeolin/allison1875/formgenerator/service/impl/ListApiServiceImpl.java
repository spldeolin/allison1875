package com.spldeolin.allison1875.formgenerator.service.impl;

import org.atteo.evo.inflector.English;
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
import com.spldeolin.allison1875.formgenerator.FormGeneratorConfig;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class ListApiServiceImpl implements ListApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private FormGeneratorConfig formGeneratorConfig;

    @Override
    public InitializerDeclaration generateListInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"list%s\", desc = \"%s列表\", form=\"%s\", type=\"%s\";",
                        English.plural(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)), ApiType.LIST.getCode())));

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
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();

        log.info("new List" + English.plural(form.getName()) + "Resp()");

        body.addStatement(StaticJavaParser.parseStatement(
                "return " + formGeneratorConfig.getPageResultEmptyConstruction() + ";"));
        return body;
    }

}
