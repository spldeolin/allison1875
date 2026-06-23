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
import com.spldeolin.allison1875.formgenerator.service.CreateApiService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.MutationApiService;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-23
 */
@Singleton
@Slf4j
public class CreateApiServiceImpl implements CreateApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private Config config;

    @Inject
    private MutationApiService mutationApiSupport;

    @Inject
    private MutationExpansionService mutationExpansionService;

    @Override
    public InitializerDeclaration generateCreateInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement("String handler = \"create%s\", desc = \"创建%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)), "create"));

        // req declaration - only fields where canInputOnInit=true, NO bizId field
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        for (ItemDef item : form.getItems()) {
            if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                // All fields get validation annotations directly (no conditional)
                itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp declaration - contains bizId field
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        FieldDeclaration bizIdField = parseFieldDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;");
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        respCoid.addMember(bizIdField);
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));

        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateCreateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();

        // 1. Entity initialization
        body.addStatement(parseStatement("%s %s = new %s();", form.getEntityName(config), form.getVarName(),
                form.getEntityName(config)));

        // 2. Set bizId
        body.addStatement(parseStatement("%s.%s(%s);", form.getVarName(), form.getBizIdSetterName(),
                config.getCodeSnippet().getShortUuidGeneration()));

        // 3. For each non-audited, non-multiSelect item where canInputOnInit=true
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                // If isNonVoid, add validation statement
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                // Call generateSetterToGetter
                mutationApiSupport.generateSetterToGetter(form, item, body);
            }
        }

        // 4. For items where canInputOnInit=false AND isNonVoid=true: set default value
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (!Boolean.TRUE.equals(item.getCanInputOnInit()) && Boolean.TRUE.equals(item.getIsNonVoid())) {
                body.addStatement(parseStatement("%s.set%s(%s);", form.getVarName(),
                        StringUtils.capitalize(item.getName()), itemService.getTodoValue(item)));
            }
        }

        // 5. Set createdAt
        body.addStatement(parseStatement("%s.setCreatedAt(LocalDateTime.now());", form.getVarName()));

        // 5.5. Expansion hook
        mutationExpansionService.expandCreateMethodBody(form, body);

        // 6. Unique index check: for indices where allCanInput(form, index, true) is true
        if (form.getIndices() != null) {
            for (IndexDef index : form.getIndices()) {
                if (Boolean.TRUE.equals(index.getIsUnique()) && mutationApiSupport.allCanInput(form, index, true)) {
                    mutationApiSupport.generateCheckExistStatement(form, index, false).forEach(body::addStatement);
                }
            }
        }

        // 7. Set updatedAt
        mutationApiSupport.generateSetUpdatedAt(form, body);

        // 8. Insert entity
        body.addStatement(parseStatement("%sMapper.insert(%s);", form.getVarName(), form.getVarName()));

        // 9. Multi-select associations
        mutationApiSupport.generateMultiSelectAssociation(form, body);

        // 10. Return response with bizId
        body.addStatement(parseStatement("return new Create%sResp().%s(%s.%s());", form.getName(),
                form.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName()));

        return body;
    }

}
