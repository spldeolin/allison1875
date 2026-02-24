package com.spldeolin.allison1875.formgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.FormGeneratorConfig;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
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

    @Inject
    private MultiSelectItemService multiSelectItemService;

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
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (item.getInitPattern() == InitOrEditPattern.USER_INPUT
                    && item.getEditPattern() == InitOrEditPattern.USER_INPUT) {
                generatorSetterToGetter(form, item, body);
            }
        }
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.setUpdatedAt(LocalDateTime.now());", form.getVarName())));
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("if (toCreate) { %sMapper.insert(%s); } else { %sMapper.updateById(%s); }",
                        form.getVarName(), form.getVarName(), form.getVarName(), form.getVarName())));

        // 删除、重新创建关联实体
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                Statement stmt = StaticJavaParser.parseStatement(
                        String.format("%sMapper.deleteBy%s(%s.%s());", associationForm.getVarName(),
                                StringUtils.capitalize(associationForm.getBizIdName()), form.getVarName(),
                                associationForm.getBizIdGetterName()));
                stmt.setLineComment(String.format("重建与%s的关联（先删除，后创建）", item.getTitle()));
                body.addStatement(stmt);
                ForEachStmt forEachStmt = new ForEachStmt();
                forEachStmt.setVariable(StaticJavaParser.parseVariableDeclarationExpr(
                        String.format("%s %s", MoreStringUtils.toUpperCamel(item.getName()) + "Enum", item.getName())));
                forEachStmt.setIterable(StaticJavaParser.parseExpression(
                        String.format("req.get%s()", StringUtils.capitalize(item.getName()))));
                BlockStmt forEachBody = new BlockStmt();
                forEachBody.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s %s = new %s();", associationForm.getName(), associationForm.getVarName(),
                                associationForm.getName())));
                forEachBody.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s.%s(%s.%s());", associationForm.getVarName(),
                                associationForm.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName())));
                forEachBody.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s.set%s(%s.getCode());", associationForm.getVarName(),
                                StringUtils.capitalize(item.getName()), item.getName())));
                forEachBody.addStatement(StaticJavaParser.parseStatement(
                        String.format("%s.setCreatedAt(LocalDateTime.now());", associationForm.getVarName())));
                forEachBody.addStatement(StaticJavaParser.parseStatement(
                        String.format("%sMapper.insert(%s);", associationForm.getVarName(),
                                associationForm.getVarName())));
                forEachStmt.setBody(forEachBody);
                body.addStatement(forEachStmt);
            }
        }

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
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (item.getInitPattern() == InitOrEditPattern.USER_INPUT && item.getEditPattern()
                    != InitOrEditPattern.USER_INPUT) { // 只有edit不为USER_INPUT，该字段才在toCreate分支内设置值
                generatorSetterToGetter(form, item, body);
            }
            if (item.getInitPattern() == InitOrEditPattern.TODO) {
                Statement stmt = StaticJavaParser.parseStatement(
                        String.format("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                                itemService.getTodoValue(item)));
                if (item.getInitPattern() == InitOrEditPattern.TODO) {
                    stmt.setLineComment("TODO 请补充初始值");
                }
                body.addStatement(stmt);
            }
            if (item.getInitPattern() == InitOrEditPattern.DO_NOT) {
                // nothing to do
            }
        }
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.setCreatedAt(LocalDateTime.now());", form.getVarName())));
        return body;
    }

    private Statement generateElseBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s = %sMapper.queryBy%s(req.%s());", form.getVarName(), form.getVarName(),
                        StringUtils.capitalize(form.getBizIdName()), form.getBizIdGetterName())));
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("if (%s == null) { throw new RuntimeException(\"%s不存在或是已被删除\"); }",
                        form.getVarName(), form.getTitle())));
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (item.getEditPattern() == InitOrEditPattern.USER_INPUT
                    && item.getInitPattern() != InitOrEditPattern.USER_INPUT) {
                generatorSetterToGetter(form, item, body);
            }
            if (item.getEditPattern() == InitOrEditPattern.TODO) {
                Statement stmt = StaticJavaParser.parseStatement(
                        String.format("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                                itemService.getTodoValue(item)));
                if (item.getEditPattern() == InitOrEditPattern.TODO) {
                    stmt.setLineComment("TODO 请补充更新值");
                }
                body.addStatement(stmt);
            }
        }
        return body;
    }

    private void generatorSetterToGetter(FormDef form, ItemDef item, BlockStmt body) {
        String getterWithConvert = String.format("req.get%s()", StringUtils.capitalize(item.getName()));
        if (item.getType() == ItemType.SELECT) {
            if (item.getIsNonVoid()) {
                getterWithConvert = getterWithConvert + ".getCode()";
            } else {
                getterWithConvert = String.format(
                        getterWithConvert + String.format("!=null ? req.get%s().getCode() : null",
                                StringUtils.capitalize(item.getName())));
            }
        }
        if (item.getType() == ItemType.TIME) {
            TimeItemDef itemItem = (TimeItemDef) item;
            if (itemItem.getFormat() == TimeFormat.DATE) {
                getterWithConvert = String.format("LocalDateTime.of(%s, LocalTime.of(0, 0))", getterWithConvert);
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                getterWithConvert = String.format("LocalDateTime.of(LocalDate.of(1970, 0, 0), %s)", getterWithConvert);
            }
        }
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                        getterWithConvert)));
    }

}
