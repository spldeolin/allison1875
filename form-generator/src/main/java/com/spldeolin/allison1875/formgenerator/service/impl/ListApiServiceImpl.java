package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseAnnotation;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseVariableDeclarationExpr;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType.MULTI_SELECT;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType.SECRET;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.PageResultGenerator;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;
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
    private Config config;

    @Inject
    private TimeItemService timeItemService;

    @Inject
    private MutationExpansionService mutationExpansionService;

    @Override
    public InitializerDeclaration generateListInitDec(FormDef form) {
        PageResultGenerator.ensureGenerated();
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement(
                "String handler = \"list%s\", desc = \"%s列表\", form=\"%s\", type=\"%s\";",
                        English.plural(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)), ApiType.LIST.getCode()));

        // req声明
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");

        // 业务主键
        FieldDeclaration itemField = parseFieldDeclaration(
                "java.util.List<String> %s;", form.getBizIdName());
        JavadocUtils.setJavadoc(itemField, "按业务主键列表过滤，null或empty代表无需过滤", null);
        reqCoid.addMember(itemField);

        // 用户字段
        for (ItemDef item : form.getNonAuditedItems()) {
            switch (item.getType()) {
                case SECRET:
                    continue;
                case NUMBER:
                case ON_OFF:
                case SELECT:
                    itemField = parseFieldDeclaration(
                            "List<" + itemService.getJavaTypeInDTO(item) + "> " + item.getName() + ";");
                    JavadocUtils.setJavadoc(itemField, "按“" + item.getTitle() + "”列表过滤，null或empty代表无需过滤",
                            null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                case MULTI_SELECT:
                    itemField = parseFieldDeclaration(
                            itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                    JavadocUtils.setJavadoc(itemField, "按“" + item.getTitle() + "”列表过滤，null或empty代表无需过滤",
                            null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                case TEXT:
                    itemField = parseFieldDeclaration(
                            itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                    JavadocUtils.setJavadoc(itemField,
                            "按“" + item.getTitle() + "”模糊匹配过滤，null或empty代表无需过滤", null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                case TIME:
                    itemField = parseFieldDeclaration(
                            "%s %sStart;", itemService.getJavaTypeInDTO(item), item.getName());
                    JavadocUtils.setJavadoc(itemField,
                            String.format("按“%s”晚于该时间过滤，null代表无需过滤", item.getTitle()), null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    itemField = parseFieldDeclaration(
                            "%s %sEnd;", itemService.getJavaTypeInDTO(item), item.getName());
                    JavadocUtils.setJavadoc(itemField,
                            String.format("按“%s”早于该时间过滤，null代表无需过滤", item.getTitle()), null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                default:
                    throw new RuntimeException("impossible");
            }
        }

        // 扩展字段
        mutationExpansionService.expandListReqFields(form, reqCoid);

        // 创建时间
        itemField = parseFieldDeclaration("java.time.LocalDateTime createdAtStart;");
        JavadocUtils.setJavadoc(itemField, "按创建时间晚于该时间过滤，null代表无需过滤", null);
        AnnotationExpr jsonFormat = parseAnnotation(
                "@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"" + TimeFormat.DATE_TIME.getPattern()
                        + "\", timezone = " + "\"Asia/Shanghai\")");
        itemField.addAnnotation(jsonFormat);
        reqCoid.addMember(itemField);
        itemField = parseFieldDeclaration("LocalDateTime createdAtEnd;");
        JavadocUtils.setJavadoc(itemField, "按创建时间早于该时间过滤，null代表无需过滤", null);
        itemField.addAnnotation(jsonFormat);
        reqCoid.addMember(itemField);

        // 分页参数
        FieldDeclaration pageNum = parseFieldDeclaration("Integer pageNum = 1;");
        JavadocUtils.setJavadoc(pageNum, "分页页码", null);
        reqCoid.addMember(pageNum);
        FieldDeclaration pageSize = parseFieldDeclaration("Integer pageSize = 10;");
        JavadocUtils.setJavadoc(pageSize, "分页条数", null);
        reqCoid.addMember(pageSize);
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // 排序方式
        itemField = parseFieldDeclaration(form.getName() + "SortEnum sortBy;");
        JavadocUtils.setJavadoc(itemField, "排序字段，null代表更新时间倒序", null);
        reqCoid.addMember(itemField);
        itemField = parseFieldDeclaration("Boolean isAsc;");
        JavadocUtils.setJavadoc(itemField, "true代表正序，否则代表倒序", null);
        reqCoid.addMember(itemField);

        // resp声明
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == SECRET) {
                continue;
            }
            itemField = parseFieldDeclaration(
                    itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
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

        String designChain = form.getName() + "Design.select(\"list" + English.plural(form.getName()) + "\").where()";
        designChain += "." + form.getBizIdName() + ".in(req." + form.getBizIdGetterName() + "())";
        for (ItemDef item : form.getItems()) {
            if (item == form.getItems().get(0)) {
                continue;
            }
            if ("updatedAt".equals(item.getName())) {
                continue;
            }
            designChain += convertItemsToSearchConditions(item);
        }
        designChain += ".page(req.getPageNum(),req.getPageSize());";
        body.addStatement(parseStatement(
                "List<" + form.getEntityName(config) + "> " + English.plural(form.getVarName()) + " = " + designChain));

        body.addStatement(parseStatement("if (%s.isEmpty()) { return %s; }", English.plural(form.getVarName()),
                "PageResult.empty()"));

        body.addStatement(
                parseStatement("List<List" + English.plural(form.getName()) + "Resp> dtos = new ArrayList<>();"));
        ForEachStmt forEachStmt = new ForEachStmt();
        forEachStmt.setVariable(
                parseVariableDeclarationExpr(String.format("%s %s", form.getEntityName(config), form.getVarName())));
        forEachStmt.setIterable(new NameExpr(English.plural(form.getVarName())));
        BlockStmt forEachBody = new BlockStmt();
        forEachBody.addStatement(parseStatement("List%sResp dto = new List%sResp();", English.plural(form.getName()),
                English.plural(form.getName())));
        for (ItemDef item : form.getItems()) {
            if (item.getType() == SECRET) {
                continue;
            }
            if (item.getType() == MULTI_SELECT) {
                // TODO
                continue;
            }
            generatorSetterToGetter(form, item, forEachBody);
        }
        mutationExpansionService.expandListSetterStatements(form, forEachBody, form.getVarName());
        forEachBody.addStatement("dtos.add(dto);");
        forEachStmt.setBody(forEachBody);
        body.addStatement(forEachStmt);
        body.addStatement(parseStatement(
                "return " + "PageResult.of(${total}, ${dtos})"
                        .replace("${total}", "list" + English.plural(form.getName()) + "Total")
                        .replace("${dtos}", "dtos") + ";"));
        return body;
    }

    private String convertItemsToSearchConditions(ItemDef item) {
        switch (item.getType()) {
            case SECRET:
            case MULTI_SELECT:
                return "";
            case NUMBER:
            case ON_OFF:
                String emptyToNull = config.getCodeSnippet().getCollectionEmptyCheck()
                        .replace("${list}", "req.get" + StringUtils.capitalize(item.getName()) + "()")
                        + " ? null : req.get" + StringUtils.capitalize(item.getName()) + "()";
                return "." + item.getName() + ".in(" + emptyToNull + ")";
            case SELECT:
                return "." + item.getName() + ".in(req.get" + StringUtils.capitalize(item.getName())
                        + "() == null ? null : req.get" + StringUtils.capitalize(item.getName()) + "().stream().map("
                        + StringUtils.capitalize(item.getName()) + "Enum::getCode).collect(Collectors.toList()))";
            case TEXT:
                return "." + item.getName() + ".like(req.get" + StringUtils.capitalize(item.getName()) + "())";
            case TIME:
                TimeItemDef timeItem = (TimeItemDef) item;
                String result;
                switch (timeItem.getFormat()) {
                    case DATE:
                        result = "." + item.getName() + ".ge(req.get" + StringUtils.capitalize(item.getName())
                                + "Start() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                item.getName()) + "Start(), LocalTime.of(0,0)))";
                        result += "." + item.getName() + ".le(req.get" + StringUtils.capitalize(item.getName())
                                + "End() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                item.getName()) + "End(), LocalTime.of(23, 59, 59)))";
                        break;
                    case TIME:
                        result = "." + item.getName() + ".ge(req.get" + StringUtils.capitalize(item.getName())
                                + "Start() == null ? null : LocalDateTime.of(LocalDate.of(1970, 1, 1), req.get"
                                + StringUtils.capitalize(item.getName()) + "Start()))";
                        result += "." + item.getName() + ".le(req.get" + StringUtils.capitalize(item.getName())
                                + "End() == null ? null : LocalDateTime.of(LocalDate.of(1970, 1, 1), req.get"
                                + StringUtils.capitalize(item.getName()) + "End()))";
                        break;
                    case DATE_TIME:
                        result = "." + item.getName() + ".ge(req.get" + StringUtils.capitalize(item.getName())
                                + "Start() == null ? null : req.get" + StringUtils.capitalize(item.getName())
                                + "Start())";
                        result += "." + item.getName() + ".le(req.get" + StringUtils.capitalize(item.getName())
                                + "End() == null ? null : req.get" + StringUtils.capitalize(item.getName()) + "End())";
                        break;
                    default:
                        throw new RuntimeException("impossible");
                }
                return result;
            default:
                throw new RuntimeException("impossible");
        }
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
                    getterWithConvert = String.format(getterWithConvert + "!=null ? %s.get%s().toLocalDate() : null",
                            form.getVarName(), StringUtils.capitalize(item.getName()));
                }
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("%s.toLocalTime()", getterWithConvert);
                } else {
                    getterWithConvert = String.format(getterWithConvert + "!=null ? %s.get%s().toLocalTime() : null",
                            form.getVarName(), StringUtils.capitalize(item.getName()));
                }
            }
        }
        body.addStatement(parseStatement(
                "dto.set%s(%s);", StringUtils.capitalize(item.getName()), getterWithConvert));
    }

}
