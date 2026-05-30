package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

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
import com.spldeolin.allison1875.formgenerator.dsl.item.SecretItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SecretItemService implements ItemService<SecretItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.SECRET;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(SecretItemDef itemDef) {
        return null;
    }

    @Override
    public Boolean isSortable(SecretItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(SecretItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(SecretItemDef itemDef) {
        return "VARCHAR(255)";
    }

    @Override
    public String getJavaTypeInDTO(SecretItemDef itemDef) {
        return "String";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(SecretItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notEmpty());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(SecretItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(SecretItemDef itemDef) {
        return "\"\"";
    }

    @Override
    public Statement getValidationStatement(SecretItemDef itemDef) {
        return parseStatement(
                "if (org.apache.commons.lang3.StringUtils.isBlank(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }

}
