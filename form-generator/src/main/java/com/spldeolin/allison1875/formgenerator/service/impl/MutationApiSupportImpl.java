package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseExpression;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseVariableDeclarationExpr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.service.MutationApiSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-23
 */
@Singleton
@Slf4j
public class MutationApiSupportImpl implements MutationApiSupport {

    @Inject
    private Config config;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public void generateSetterToGetter(FormDef form, ItemDef item, BlockStmt body) {
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
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("LocalDateTime.of(%s, LocalTime.of(0, 0))", getterWithConvert);
                } else {
                    getterWithConvert = String.format("%s != null ? LocalDateTime.of(%s, LocalTime.of(0, 0)) : null",
                            getterWithConvert, getterWithConvert);
                }
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("LocalDateTime.of(LocalDate.of(1970, 1, 1), %s)",
                            getterWithConvert);
                } else {
                    getterWithConvert = String.format(
                            "%s != null ? LocalDateTime.of(LocalDate.of(1970, 1, 1), %s) : null", getterWithConvert,
                            getterWithConvert);
                }
            }
        }
        body.addStatement(parseStatement("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                getterWithConvert));
    }

    @Override
    public List<Statement> generateCheckExistStatement(FormDef form, IndexDef index, boolean isUpdate) {
        // 构造标题：由 index.itemNames 对应的字段 title 拼接而成
        List<String> fieldTitles = index.getItemNames().stream()
                .map(itemName -> form.getItems().stream().filter(item -> item.getName().equals(itemName)).findFirst()
                        .map(ItemDef::getTitle).orElse(itemName)).collect(java.util.stream.Collectors.toList());
        String conflictDesc = String.join("、", fieldTitles) + "已存在";

        // 构造 Design chain：
        //   {Entity}Design.select().where()
        //     .ne(req.getBizId())          ← 排除自身（仅编辑场景），bizId 可能为 null 所以用 ne
        //     .eq(field1, req.getField1()) ← 每个 index 字段
        //     .one();
        StringBuilder chain = new StringBuilder();
        chain.append(form.getName()).append("Design.select().where()");
        if (isUpdate) {
            // Only exclude self when updating (create doesn't have bizId in req)
            chain.append(".").append(form.getBizIdName()).append(".ne(req.").append(form.getBizIdGetterName())
                    .append("())");
        }
        Map<String, ItemDef> items = form.getItems().stream().collect(Collectors.toMap(ItemDef::getName, item -> item));
        for (String itemName : index.getItemNames()) {
            chain.append(convertItemsToSearchConditions(items.get(itemName)));
        }
        chain.append(".one()");

        String varName = "exist" + form.getName() + "For" + index.getItemNames().stream().map(StringUtils::capitalize)
                .collect(java.util.stream.Collectors.joining());

        List<Statement> statements = Lists.newArrayList();
        statements.add(parseStatement("%s %s = %s;", form.getEntityName(config), varName, chain));
        statements.add(parseStatement("if (%s != null) { throw new %s(\"%s\"); }", varName,
                config.getCodeSnippet().getBizExceptionQualifier(), conflictDesc));
        return statements;
    }

    @Override
    public boolean allCanInput(FormDef form, IndexDef index, boolean onInit) {
        return index.getItemNames().stream().allMatch(
                n -> form.getItems().stream().filter(i -> i.getName().equals(n)).findFirst()
                        .map(i -> onInit ? Boolean.TRUE.equals(i.getCanInputOnInit())
                                : Boolean.TRUE.equals(i.getCanInputOnEdit())).orElse(false));
    }

    @Override
    public void generateMultiSelectAssociation(FormDef form, BlockStmt body) {
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                Statement stmt = parseStatement("%sMapper.deleteBy%s(%s.%s());", associationForm.getVarName(),
                        StringUtils.capitalize(associationForm.getBizIdName()), form.getVarName(),
                        associationForm.getBizIdGetterName());
                stmt.setLineComment(String.format("重建与%s的关联（先删除，后创建）", item.getTitle()));
                body.addStatement(stmt);
                ForEachStmt forEachStmt = new ForEachStmt();
                forEachStmt.setVariable(parseVariableDeclarationExpr(
                        String.format("%s %s", MoreStringUtils.toUpperCamel(item.getName()) + "Enum", item.getName())));
                forEachStmt.setIterable(
                        parseExpression(String.format("req.get%s()", StringUtils.capitalize(item.getName()))));
                BlockStmt forEachBody = new BlockStmt();
                forEachBody.addStatement(parseStatement("%s %s = new %s();", associationForm.getEntityName(config),
                        associationForm.getVarName(), associationForm.getEntityName(config)));
                forEachBody.addStatement(parseStatement("%s.%s(%s.%s());", associationForm.getVarName(),
                        associationForm.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName()));
                forEachBody.addStatement(parseStatement("%s.set%s(%s.getCode());", associationForm.getVarName(),
                        StringUtils.capitalize(item.getName()), item.getName()));
                forEachBody.addStatement(
                        parseStatement("%s.setCreatedAt(LocalDateTime.now());", associationForm.getVarName()));
                forEachBody.addStatement(parseStatement("%sMapper.insert(%s);", associationForm.getVarName(),
                        associationForm.getVarName()));
                forEachStmt.setBody(forEachBody);
                body.addStatement(forEachStmt);
            }
        }
    }

    @Override
    public void generateSetUpdatedAt(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setUpdatedAt(LocalDateTime.now());", form.getVarName()));
    }

    private String convertItemsToSearchConditions(ItemDef item) {
        switch (item.getType()) {
            case MULTI_SELECT:
                return "";
            case SECRET:
            case NUMBER:
            case ON_OFF:
            case TEXT:
                return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName()) + "())";
            case TIME:
                TimeItemDef timeItem = (TimeItemDef) item;
                switch (timeItem.getFormat()) {
                    case DATE:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                                + "() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                item.getName()) + "(), LocalTime.of(0, 0)))";
                    case TIME:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                                + "() == null ? null : LocalDateTime.of(LocalDate.of(1970, 1, 1), req.get"
                                + StringUtils.capitalize(item.getName()) + "()))";
                    case DATE_TIME:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName()) + "())";
                    default:
                        throw new RuntimeException("impossible");
                }
            case SELECT:
                return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                        + "() == null ? null : req.get" + StringUtils.capitalize(item.getName()) + "().getCode())";
            default:
                throw new RuntimeException("impossible");
        }
    }

}
