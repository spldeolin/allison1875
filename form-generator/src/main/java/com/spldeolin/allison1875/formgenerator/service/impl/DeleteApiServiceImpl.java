package com.spldeolin.allison1875.formgenerator.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class DeleteApiServiceImpl implements DeleteApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public InitializerDeclaration generateDeleteInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"delete%s\", desc = \"删除%s\", form=\"%s\", type=\"%s\";",
                        form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                        ApiType.DELETE.getCode())));

        // req声明
        FieldDeclaration bizIdField = StaticJavaParser.parseBodyDeclaration(
                "java.util.List<String> " + form.getBizIdName() + "s;").asFieldDeclaration();
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req")
                .addMember(bizIdField.clone().addAnnotation(annotationExprService.notEmpty()));
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                form.getName() + "Design.delete().where()." + form.getBizIdName() + ".in(req."
                        + form.getBizIdGetterName() + "s()).over();"));
        form.getItems().stream().filter(item -> item.getType() == ItemType.MULTI_SELECT)
                .map(item -> ((MultiSelectItemDef) item)).forEach(multiSelectItem -> {
                    FormDef associationForm = multiSelectItemService.toAssociationForm(form, multiSelectItem);
                    body.addStatement(StaticJavaParser.parseStatement(
                            String.format("%sDesign.delete().where().%s.in(req.%ss()).over();",
                                    associationForm.getName(),
                                    associationForm.getBizIdName(), associationForm.getBizIdGetterName())));
                });
        return body;
    }

}
