package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.MutationApiService;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;
import com.spldeolin.allison1875.formgenerator.service.UpdateApiService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-23
 */
@Singleton
@Slf4j
public class UpdateApiServiceImpl implements UpdateApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private Config config;

    @Inject
    private MutationApiService mutationApiSupport;

    @Inject
    private MutationExpansionService mutationExpansionService;

    @Override
    public InitializerDeclaration generateUpdateInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement("String handler = \"update%s\", desc = \"更新%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)), "update"));

        // req declaration: bizId field FIRST, then canInputOnEdit fields
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = parseFieldDeclaration(
                "@javax.validation.constraints.NotNull String " + StringUtils.uncapitalize(form.getName()) + "Code;");
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            // 字段进入 ReqDTO 当且仅当编辑时允许用户输入
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                // secret 字段编辑时 null 代表"不修改"，因此不加 @NotEmpty；其余字段照常
                if (item.getType() != ItemType.SECRET) {
                    itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                }
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // No resp declaration — absence of resp signals void method to handler-transformer
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateUpdateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();

        // 1. Query entity by bizId
        body.addStatement(parseStatement("%s %s = %sMapper.queryBy%s(req.%s());", form.getEntityName(config),
                form.getVarName(), form.getVarName(), StringUtils.capitalize(form.getBizIdName()),
                form.getBizIdGetterName()));

        // 2. Existence check
        body.addStatement(
                parseStatement("if (%s == null) { throw new %s(\"%s不存在或是已被删除\"); }", form.getVarName(),
                        config.getCodeSnippet().getBizExceptionQualifier(), form.getTitle()));

        // 3. Set fields where canInputOnEdit=true (non-audited, non-multiSelect)
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                mutationApiSupport.generateSetterToGetter(form, item, body);
            }
        }

        // 4. Set updatedAt
        mutationApiSupport.generateSetUpdatedAt(form, body);

        // 5. Expansion hook
        mutationExpansionService.expandUpdateMethodBody(form, body);

        // 6. Unique index check: only for indices where all fields canInputOnEdit
        if (form.getIndices() != null) {
            for (IndexDef index : form.getIndices()) {
                if (Boolean.TRUE.equals(index.getIsUnique()) && mutationApiSupport.allCanInput(form, index, false)) {
                    mutationApiSupport.generateCheckExistStatement(form, index, true).forEach(body::addStatement);
                }
            }
        }

        // 7. Update by ID
        body.addStatement(parseStatement("%sMapper.updateById(%s);", form.getVarName(), form.getVarName()));

        // 8. MultiSelect association
        mutationApiSupport.generateMultiSelectAssociation(form, body);

        // 8. Post-process hook (audit log wrapping, etc.)
        mutationExpansionService.postProcessMethodBody(form, body, "update");

        // No return statement (void method)
        return body;
    }

}
