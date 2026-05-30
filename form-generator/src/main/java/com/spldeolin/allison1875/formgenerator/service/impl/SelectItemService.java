package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SelectItemService implements ItemService<SelectItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private Config config;

    @Override
    public ItemType supportedItemType() {
        return ItemType.SELECT;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(SelectItemDef itemDef) {
        return Lists.newArrayList(IN);
    }

    @Override
    public Boolean isSortable(SelectItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(SelectItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(SelectItemDef itemDef) {
        return "VARCHAR(64)";
    }

    @Override
    public String getJavaTypeInDTO(SelectItemDef itemDef) {
        return DomainContext.get().getEnumPackage() + "." + MoreStringUtils.toUpperCamel(itemDef.getName()) + "Enum";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(SelectItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notNull());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(SelectItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(SelectItemDef itemDef) {
        return MoreStringUtils.splitAndGetLastPart(getJavaTypeInDTO(itemDef), ".") + "." + itemDef.getOptions().get(0)
                .javaEnumConstantName() + ".getCode()";
    }

    @Override
    public Statement getValidationStatement(SelectItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }

}
