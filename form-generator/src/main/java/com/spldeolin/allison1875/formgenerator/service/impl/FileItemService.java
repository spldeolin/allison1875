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
import com.spldeolin.allison1875.formgenerator.dsl.item.FileItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-07-02
 */
@Singleton
public class FileItemService implements ItemService<FileItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.FILE;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(FileItemDef itemDef) {
        return null;
    }

    @Override
    public Boolean isSortable(FileItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(FileItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(FileItemDef itemDef) {
        return "VARCHAR(512)";
    }

    @Override
    public String getJavaTypeInDTO(FileItemDef itemDef) {
        return "String";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(FileItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notEmpty());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(FileItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(FileItemDef itemDef) {
        return "\"\"";
    }

    @Override
    public Statement getValidationStatement(FileItemDef itemDef) {
        return parseStatement(
                "if (!StringUtils.hasText(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }

}
