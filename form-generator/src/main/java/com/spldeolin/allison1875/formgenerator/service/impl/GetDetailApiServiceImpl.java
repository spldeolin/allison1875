package com.spldeolin.allison1875.formgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class GetDetailApiServiceImpl implements GetDetailApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public InitializerDeclaration generateGetDetailInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"get%sDetail\", desc = \"%s详情\", form=\"%s\", type=\"%s\";",
                        form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                        ApiType.GET_DETAIL.getCode())));

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
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        Statement stmt = StaticJavaParser.parseStatement(
                String.format("%s %s = %sMapper.queryBy%s(req.%s());", form.getName(), form.getVarName(),
                        form.getVarName(), StringUtils.capitalize(form.getBizIdName()), form.getBizIdGetterName()));
        stmt.setLineComment("查询" + form.getTitle());
        body.addStatement(stmt);
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("if (%s == null) { throw new RuntimeException(\"%s不存在或是已被删除\"); }",
                        form.getVarName(), form.getTitle())));

        // 为每个多选字段查询关联表单
        form.getItems().stream().filter(item -> item.getType() == ItemType.MULTI_SELECT)
                .map(item -> ((MultiSelectItemDef) item)).forEach(multiSelectItem -> {
                    FormDef associationForm = multiSelectItemService.toAssociationForm(form, multiSelectItem);
                    String enumName = StringUtils.capitalize(multiSelectItem.getName()) + "Enum";
                    Statement statement = StaticJavaParser.parseStatement(String.format(
                            "List<%s> %s = %sMapper.queryBy%s(%s.%s()).stream().map(%s::get%s).map(%s::of).collect"
                                    + "(Collectors.toList());", enumName, multiSelectItem.getName(),
                            associationForm.getVarName(), StringUtils.capitalize(form.getBizIdName()),
                            form.getVarName(),
                            form.getBizIdGetterName(), associationForm.getName(),
                            StringUtils.capitalize(multiSelectItem.getName()), enumName));
                    statement.setLineComment("查询" + associationForm.getTitle());
                    body.addStatement(statement);
                });

        stmt = StaticJavaParser.parseStatement(
                String.format("Get%sDetailResp result = new Get%sDetailResp();", form.getName(), form.getName()));
        stmt.setLineComment("构建返回值");
        body.addStatement(stmt);
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.SECRET) {
                // 密码、密钥类不应返回
                continue;
            }
            if (item.getType() == ItemType.MULTI_SELECT) {
                body.addStatement(StaticJavaParser.parseStatement(
                        String.format("result.set%s(%s);", StringUtils.capitalize(item.getName()), item.getName())));
                continue;
            }
            generatorSetterToGetter(form, item, body);
        }
        body.addStatement(StaticJavaParser.parseStatement("return result;"));
        return body;
    }

    private void generatorSetterToGetter(FormDef form, ItemDef item, BlockStmt body) {
        String getterWithConvert = String.format("%s.get%s()", form.getVarName(),
                StringUtils.capitalize(item.getName()));
        if (item.getType() == ItemType.SELECT) {
            getterWithConvert = String.format("%s.of(%s)", StringUtils.capitalize(item.getName()) + "Enum",
                    getterWithConvert);
        }
        if (item.getType() == ItemType.TIME) {
            TimeItemDef itemItem = (TimeItemDef) item;
            if (itemItem.getFormat() == TimeFormat.DATE) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("%s.toLocalDate()", getterWithConvert);
                } else {
                    getterWithConvert = String.format(
                            getterWithConvert + String.format("!=null ? %s.toLocalDate() : null",
                                    StringUtils.capitalize(item.getName())));
                }
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("%s.toLocalTime()", getterWithConvert);
                } else {
                    getterWithConvert = String.format(
                            getterWithConvert + String.format("!=null ? %s.toLocalTime() : null",
                                    StringUtils.capitalize(item.getName())));
                }
            }
        }
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("result.set%s(%s);", StringUtils.capitalize(item.getName()), getterWithConvert)));
    }

}
