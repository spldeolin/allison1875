package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.service.impl.MutationApiServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(MutationApiServiceImpl.class)
public interface MutationApiService {

    void generateSetterToGetter(FormDef form, ItemDef item, BlockStmt body);

    List<Statement> generateCheckExistStatement(FormDef form, IndexDef index, boolean isUpdate);

    boolean allCanInput(FormDef form, IndexDef index, boolean onInit);

    void generateMultiSelectAssociation(FormDef form, BlockStmt body);

    void generateSetUpdatedAt(FormDef form, BlockStmt body);

}
