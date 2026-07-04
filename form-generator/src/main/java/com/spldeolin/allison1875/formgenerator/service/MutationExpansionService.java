package com.spldeolin.allison1875.formgenerator.service;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMutationExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(FormGeneratorMutationExpansionServiceImpl.class)
public interface MutationExpansionService {

    void expandCreateMethodBody(FormDef form, BlockStmt body);

    void expandUpdateMethodBody(FormDef form, BlockStmt body);

    void expandDeleteMethodBody(FormDef form, BlockStmt body);

    void expandListReqFields(FormDef form, ClassOrInterfaceDeclaration reqCoid);

    void expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName);

    default void postProcessMethodBody(FormDef form, BlockStmt body, String apiType) {
    }

    /**
     * 在 multiSelect 关联记录的 forEach 循环体内追加语句（如 setCreatedBy）。
     *
     * 默认空实现；app-generator 等下游工具可覆写以注入 CurrentUser 等依赖。
     */
    default void expandMultiSelectAssociationBody(FormDef majorForm, ItemDef multiSelectItem,
            FormDef associationForm, BlockStmt forEachBody) {
    }

    default List<FieldDeclaration> expandServiceImplFields(FormDef form) {
        return Collections.emptyList();
    }

}
