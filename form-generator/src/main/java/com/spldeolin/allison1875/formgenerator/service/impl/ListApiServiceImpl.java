package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType.SECRET;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
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

    @Inject
    private TimeItemService timeItemService;

    @Override
    public InitializerDeclaration generateListInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(StaticJavaParser.parseStatement(
                String.format("String handler = \"list%s\", desc = \"%s列表\", form=\"%s\", type=\"%s\";",
                        English.plural(form.getName()), form.getTitle(),
                        StringEscapeUtils.escapeJava(JsonUtils.toJson(form)), ApiType.LIST.getCode())));

        // req声明
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");

        // 业务主键
        FieldDeclaration itemField = StaticJavaParser.parseBodyDeclaration(
                String.format("java.util.List<String> %s;", form.getBizIdName())).asFieldDeclaration();
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
                case MULTI_SELECT:
                    itemField = StaticJavaParser.parseBodyDeclaration(
                                    "List<" + itemService.getJavaTypeInDTO(item) + "> " + item.getName() + ";")
                            .asFieldDeclaration();
                    JavadocUtils.setJavadoc(itemField, "按“" + item.getTitle() + "”列表过滤，null或empty代表无需过滤",
                            null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                case TEXT:
                    itemField = StaticJavaParser.parseBodyDeclaration(
                            itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";").asFieldDeclaration();
                    JavadocUtils.setJavadoc(itemField,
                            "按“" + item.getTitle() + "”模糊匹配过滤，null或empty代表无需过滤", null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                case TIME:
                    itemField = StaticJavaParser.parseBodyDeclaration(
                                    String.format("%s %sStart;", itemService.getJavaTypeInDTO(item), item.getName()))
                            .asFieldDeclaration();
                    JavadocUtils.setJavadoc(itemField,
                            String.format("按“%s”晚于该时间过滤，null代表无需过滤", item.getTitle()), null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    itemField = StaticJavaParser.parseBodyDeclaration(
                                    String.format("%s %sEnd;", itemService.getJavaTypeInDTO(item), item.getName()))
                            .asFieldDeclaration();
                    JavadocUtils.setJavadoc(itemField,
                            String.format("按“%s”早于该时间过滤，null代表无需过滤", item.getTitle()), null);
                    itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                    reqCoid.addMember(itemField);
                    break;
                default:
                    throw new RuntimeException("impossible");
            }
        }

        // 创建时间
        itemField = StaticJavaParser.parseBodyDeclaration("java.time.LocalDateTime createdAtStart;")
                .asFieldDeclaration();
        JavadocUtils.setJavadoc(itemField, "按创建时间晚于该时间过滤，null代表无需过滤", null);
        AnnotationExpr jsonFormat = StaticJavaParser.parseAnnotation(
                "@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"" + TimeFormat.DATE_TIME.getPattern()
                        + "\", timezone = " + "\"Asia/Shanghai\")");
        itemField.addAnnotation(jsonFormat);
        reqCoid.addMember(itemField);
        itemField = StaticJavaParser.parseBodyDeclaration("LocalDateTime createdAtEnd;").asFieldDeclaration();
        JavadocUtils.setJavadoc(itemField, "按创建时间早于该时间过滤，null代表无需过滤", null);
        itemField.addAnnotation(jsonFormat);
        reqCoid.addMember(itemField);

        // 分页参数
        FieldDeclaration pageNum = StaticJavaParser.parseBodyDeclaration("Integer pageNum = 1;").asFieldDeclaration();
        JavadocUtils.setJavadoc(pageNum, "分页页码", null);
        reqCoid.addMember(pageNum);
        FieldDeclaration pageSize = StaticJavaParser.parseBodyDeclaration("Integer pageSize = 10;")
                .asFieldDeclaration();
        JavadocUtils.setJavadoc(pageSize, "分页条数", null);
        reqCoid.addMember(pageSize);
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // TODO 排序规则

        // resp声明
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == SECRET) {
                continue;
            }
            itemField = StaticJavaParser.parseBodyDeclaration(
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

        String designChain = form.getName() + "Design.select().where()";
        designChain += "." + form.getBizIdName() + ".in(req." + form.getBizIdGetterName() + "())";
        for (ItemDef item : form.getItems().subList(1, form.getItems().size())) {
            switch (item.getType()) {
                case SECRET:
                case MULTI_SELECT:
                    continue;
                case NUMBER:
                case ON_OFF:
                    String emptyToNull = formGeneratorConfig.getCollectionEmptyCheck()
                            .replace("${list}", "req.get" + StringUtils.capitalize(item.getName()) + "()")
                            + " ? null : req.get" + StringUtils.capitalize(item.getName()) + "()";
                    designChain += "." + item.getName() + ".in(" + emptyToNull + ")";
                    break;
                case SELECT:
                    designChain += "." + item.getName() + ".in(req.get" + StringUtils.capitalize(item.getName())
                            + "() == null ? null : req.get" + StringUtils.capitalize(item.getName())
                            + "().stream().map(" + StringUtils.capitalize(item.getName())
                            + "Enum::getCode).collect(Collectors.toList()))";
                    break;
                case TEXT:
                    designChain +=
                            "." + item.getName() + ".like(req.get" + StringUtils.capitalize(item.getName()) + "())";
                    break;
                case TIME:
                    TimeItemDef timeItem = (TimeItemDef) item;
                    switch (timeItem.getFormat()) {
                        case DATE:
                            designChain += "." + item.getName() + ".ge(req.get" + StringUtils.capitalize(item.getName())
                                    + "Start() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                    item.getName()) + "Start(), LocalTime.of(0,0)))";
                            designChain += "." + item.getName() + ".le(req.get" + StringUtils.capitalize(item.getName())
                                    + "End() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                    item.getName()) + "End(), LocalTime.of(0,0)))";
                            break;
                        case TIME:
                            break;
                        case DATE_TIME:
                            break;
                        default:
                            throw new RuntimeException("impossible");
                    }
                    break;
                default:
                    throw new RuntimeException("impossible");
            }
        }
        designChain += ".page(req.getPageNum(),req.getPageSize());";

        body.addStatement(StaticJavaParser.parseStatement(designChain));
        body.addStatement(StaticJavaParser.parseStatement(
                "return " + formGeneratorConfig.getPageResultEmptyConstruction() + ";"));
        return body;
    }

}
