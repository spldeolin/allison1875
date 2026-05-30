package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseExpression;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseVariableDeclarationExpr;

import org.apache.commons.lang3.StringUtils;
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
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
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
    private Config config;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public InitializerDeclaration generateSaveInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement(
                "String handler = \"save%s\", desc = \"创建%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                ApiType.SAVE.getCode()));

        // req declaration
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = parseFieldDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;");
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            // 字段进入 ReqDTO 当且仅当 init 或 edit 任一允许用户输入
            if (Boolean.TRUE.equals(item.getCanInputOnInit()) || Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                // 仅在 (true,true) 组合时把 isNonVoid 校验注解放在 ReqDTO 字段上；
                // 其他组合的 isNonVoid 校验改为分支内 if-throw（见 generateMethodBody/IfThen/Else）。
                if (Boolean.TRUE.equals(item.getCanInputOnInit())
                        && Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                    itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                }
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp declaration
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp").addMember(bizIdField);
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "boolean toCreate = req.%s() == null;", form.getBizIdGetterName()));
        body.addStatement(parseStatement(
                "%s %s;", form.getEntityName(config), form.getVarName()));
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(new NameExpr("toCreate"));
        ifStmt.setThenStmt(generateIfThenBody(form));
        ifStmt.setElseStmt(generateElseBody(form));
        body.addStatement(ifStmt);

        // common section: only (canInputOnInit=true, canInputOnEdit=true) 字段在此设置
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnInit())
                    && Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                generatorSetterToGetter(form, item, body);
            }
        }
        body.addStatement(parseStatement(
                "%s.setUpdatedAt(LocalDateTime.now());", form.getVarName()));
        body.addStatement(parseStatement(
                "if (toCreate) { %sMapper.insert(%s); } else { %sMapper.updateById(%s); }",
                form.getVarName(), form.getVarName(), form.getVarName(), form.getVarName()));

        // 删除、重新创建关联实体（multiSelect 路径不变）
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                Statement stmt = parseStatement(
                        "%sMapper.deleteBy%s(%s.%s());", associationForm.getVarName(),
                        StringUtils.capitalize(associationForm.getBizIdName()), form.getVarName(),
                        associationForm.getBizIdGetterName());
                stmt.setLineComment(String.format("重建与%s的关联（先删除，后创建）", item.getTitle()));
                body.addStatement(stmt);
                ForEachStmt forEachStmt = new ForEachStmt();
                forEachStmt.setVariable(parseVariableDeclarationExpr(
                        String.format("%s %s", MoreStringUtils.toUpperCamel(item.getName()) + "Enum", item.getName())));
                forEachStmt.setIterable(parseExpression(
                        String.format("req.get%s()", StringUtils.capitalize(item.getName()))));
                BlockStmt forEachBody = new BlockStmt();
                forEachBody.addStatement(parseStatement("%s %s = new %s();", associationForm.getEntityName(config),
                        associationForm.getVarName(), associationForm.getEntityName(config)));
                forEachBody.addStatement(parseStatement(
                        "%s.%s(%s.%s());", associationForm.getVarName(),
                        associationForm.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName()));
                forEachBody.addStatement(parseStatement(
                        "%s.set%s(%s.getCode());", associationForm.getVarName(),
                        StringUtils.capitalize(item.getName()), item.getName()));
                forEachBody.addStatement(parseStatement(
                        "%s.setCreatedAt(LocalDateTime.now());", associationForm.getVarName()));
                forEachBody.addStatement(parseStatement(
                        "%sMapper.insert(%s);", associationForm.getVarName(),
                        associationForm.getVarName()));
                forEachStmt.setBody(forEachBody);
                body.addStatement(forEachStmt);
            }
        }

        body.addStatement(parseStatement(
                "return new Save" + form.getName() + "Resp()." + form.getBizIdSetterName() + "(" + form.getVarName()
                        + "." + form.getBizIdGetterName() + "());"));
        return body;
    }

    private Statement generateIfThenBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "%s = new %s();", form.getVarName(), form.getEntityName(config)));
        body.addStatement(parseStatement(
                "%s.%s(%s);", form.getVarName(), form.getBizIdSetterName(),
                config.getCodeSnippet().getShortUuidGeneration()));
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            boolean init = Boolean.TRUE.equals(item.getCanInputOnInit());
            boolean edit = Boolean.TRUE.equals(item.getCanInputOnEdit());

            if (init && !edit) {
                // case (true,false): if isNonVoid → if-throw, then setter
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                generatorSetterToGetter(form, item, body);
            } else if (!init) {
                // case (false,true) and (false,false): non-void → default value
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(parseStatement(
                            "%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                            itemService.getTodoValue(item)));
                }
            }
        }
        body.addStatement(parseStatement(
                "%s.setCreatedAt(LocalDateTime.now());", form.getVarName()));
        return body;
    }

    private Statement generateElseBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "%s = %sMapper.queryBy%s(req.%s());", form.getVarName(), form.getVarName(),
                StringUtils.capitalize(form.getBizIdName()), form.getBizIdGetterName()));
        body.addStatement(parseStatement(
                "if (%s == null) { throw new RuntimeException(\"%s不存在或是已被删除\"); }",
                form.getVarName(), form.getTitle()));
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            boolean init = Boolean.TRUE.equals(item.getCanInputOnInit());
            boolean edit = Boolean.TRUE.equals(item.getCanInputOnEdit());
            if (!init && edit) {
                // case (false,true): if isNonVoid → if-throw, then setter
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                generatorSetterToGetter(form, item, body);
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
        body.addStatement(parseStatement(
                "%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                getterWithConvert));
    }

}
