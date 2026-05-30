package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.GE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.GT;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LT;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.NumberItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class NumberItemService implements ItemService<NumberItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.NUMBER;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(NumberItemDef itemDef) {
        return Lists.newArrayList(IN, GE, GT, LE, LT);
    }

    @Override
    public Boolean isSortable(NumberItemDef itemDef) {
        return true;
    }

    @Override
    public String getDbColumnName(NumberItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(NumberItemDef itemDef) {
        return itemDef.getCanBeDecimal() ? "DECIMAL(14, 4)" : "BIGINT";
    }

    @Override
    public String getJavaTypeInDTO(NumberItemDef itemDef) {
        return itemDef.getCanBeDecimal() ? "java.math.BigDecimal" : "Long";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(NumberItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notNull());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(NumberItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(NumberItemDef itemDef) {
        return itemDef.getCanBeDecimal() ? "new BigDecimal(0)" : "0L";
    }

    @Override
    public Statement getValidationStatement(NumberItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }

}
