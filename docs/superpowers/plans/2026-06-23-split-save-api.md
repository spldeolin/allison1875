# Split SaveApiService into CreateApiService + UpdateApiService — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split the combined save API generation into independent create and update APIs with proper permission isolation.

**Architecture:** Extract shared logic into `MutationApiSupport`, then implement `CreateApiService` and `UpdateApiService` as independent Guice singletons. Adapt `FormGenerator`, `ApiType`, `ServiceLayerExpansionService`, `ControllerAuthAnnotateService`, and frontend skeleton accordingly.

**Tech Stack:** Java 21, Google Guice, JavaParser 3.28.1, TypeScript (Vue 3 frontend skeleton)

---

### Task 1: Create MutationApiSupport interface and implementation

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/MutationApiSupport.java`
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MutationApiSupportImpl.java`

- [ ] **Step 1: Create the MutationApiSupport interface**

```java
package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.service.impl.MutationApiSupportImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(MutationApiSupportImpl.class)
public interface MutationApiSupport {

    void generateSetterToGetter(FormDef form, ItemDef item, BlockStmt body);

    List<Statement> generateCheckExistStatement(FormDef form, IndexDef index);

    boolean allCanInput(FormDef form, IndexDef index, boolean onInit);

    void generateMultiSelectAssociation(FormDef form, BlockStmt body);

    void generateSetUpdatedAt(FormDef form, BlockStmt body);

}
```

- [ ] **Step 2: Create the MutationApiSupportImpl implementation**

Extract shared methods from `SaveApiServiceImpl` into this class. The implementation:

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseExpression;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseVariableDeclarationExpr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.MutationApiSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-23
 */
@Singleton
@Slf4j
public class MutationApiSupportImpl implements MutationApiSupport {

    @Inject
    private Config config;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public void generateSetterToGetter(FormDef form, ItemDef item, BlockStmt body) {
        String getterWithConvert = String.format("req.get%s()", StringUtils.capitalize(item.getName()));
        if (item.getType() == ItemType.SELECT) {
            if (item.getIsNonVoid()) {
                getterWithConvert = getterWithConvert + ".getCode()";
            } else {
                getterWithConvert = String.format(
                        getterWithConvert + String.format("!=null ? req.get%s().getCode() : null",
                                StringUtils.capitalize(item.getName())));
            }
        }
        if (item.getType() == ItemType.TIME) {
            TimeItemDef itemItem = (TimeItemDef) item;
            if (itemItem.getFormat() == TimeFormat.DATE) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("LocalDateTime.of(%s, LocalTime.of(0, 0))", getterWithConvert);
                } else {
                    getterWithConvert = String.format("%s != null ? LocalDateTime.of(%s, LocalTime.of(0, 0)) : null",
                            getterWithConvert, getterWithConvert);
                }
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                if (item.getIsNonVoid()) {
                    getterWithConvert = String.format("LocalDateTime.of(LocalDate.of(1970, 1, 1), %s)",
                            getterWithConvert);
                } else {
                    getterWithConvert = String.format(
                            "%s != null ? LocalDateTime.of(LocalDate.of(1970, 1, 1), %s) : null", getterWithConvert,
                            getterWithConvert);
                }
            }
        }
        body.addStatement(parseStatement("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                getterWithConvert));
    }

    @Override
    public List<Statement> generateCheckExistStatement(FormDef form, IndexDef index) {
        List<String> fieldTitles = index.getItemNames().stream()
                .map(itemName -> form.getItems().stream().filter(item -> item.getName().equals(itemName)).findFirst()
                        .map(ItemDef::getTitle).orElse(itemName)).collect(java.util.stream.Collectors.toList());
        String conflictDesc = String.join("、", fieldTitles) + "已存在";

        StringBuilder chain = new StringBuilder();
        chain.append(form.getName()).append("Design.select().where()");
        chain.append(".").append(form.getBizIdName()).append(".ne(req.").append(form.getBizIdGetterName())
                .append("())");
        Map<String, ItemDef> items = form.getItems().stream().collect(Collectors.toMap(ItemDef::getName, item -> item));
        for (String itemName : index.getItemNames()) {
            chain.append(convertItemsToSearchConditions(items.get(itemName)));
        }
        chain.append(".one()");

        String varName = "exist" + form.getName() + "For" + index.getItemNames().stream().map(StringUtils::capitalize)
                .collect(java.util.stream.Collectors.joining());

        List<Statement> statements = Lists.newArrayList();
        statements.add(parseStatement("%s %s = %s;", form.getEntityName(config), varName, chain));
        statements.add(parseStatement("if (%s != null) { throw new %s(\"%s\"); }", varName,
                config.getCodeSnippet().getBizExceptionQualifier(), conflictDesc));
        return statements;
    }

    @Override
    public boolean allCanInput(FormDef form, IndexDef index, boolean onInit) {
        return index.getItemNames().stream().allMatch(
                n -> form.getItems().stream().filter(i -> i.getName().equals(n)).findFirst()
                        .map(i -> onInit ? Boolean.TRUE.equals(i.getCanInputOnInit())
                                : Boolean.TRUE.equals(i.getCanInputOnEdit())).orElse(false));
    }

    @Override
    public void generateMultiSelectAssociation(FormDef form, BlockStmt body) {
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                Statement stmt = parseStatement("%sMapper.deleteBy%s(%s.%s());", associationForm.getVarName(),
                        StringUtils.capitalize(associationForm.getBizIdName()), form.getVarName(),
                        associationForm.getBizIdGetterName());
                stmt.setLineComment(String.format("重建与%s的关联（先删除，后创建）", item.getTitle()));
                body.addStatement(stmt);
                ForEachStmt forEachStmt = new ForEachStmt();
                forEachStmt.setVariable(parseVariableDeclarationExpr(
                        String.format("%s %s", MoreStringUtils.toUpperCamel(item.getName()) + "Enum", item.getName())));
                forEachStmt.setIterable(
                        parseExpression(String.format("req.get%s()", StringUtils.capitalize(item.getName()))));
                BlockStmt forEachBody = new BlockStmt();
                forEachBody.addStatement(parseStatement("%s %s = new %s();", associationForm.getEntityName(config),
                        associationForm.getVarName(), associationForm.getEntityName(config)));
                forEachBody.addStatement(parseStatement("%s.%s(%s.%s());", associationForm.getVarName(),
                        associationForm.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName()));
                forEachBody.addStatement(parseStatement("%s.set%s(%s.getCode());", associationForm.getVarName(),
                        StringUtils.capitalize(item.getName()), item.getName()));
                forEachBody.addStatement(
                        parseStatement("%s.setCreatedAt(LocalDateTime.now());", associationForm.getVarName()));
                forEachBody.addStatement(parseStatement("%sMapper.insert(%s);", associationForm.getVarName(),
                        associationForm.getVarName()));
                forEachStmt.setBody(forEachBody);
                body.addStatement(forEachStmt);
            }
        }
    }

    @Override
    public void generateSetUpdatedAt(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setUpdatedAt(LocalDateTime.now());", form.getVarName()));
    }

    private String convertItemsToSearchConditions(ItemDef item) {
        switch (item.getType()) {
            case MULTI_SELECT:
                return "";
            case SECRET:
            case NUMBER:
            case ON_OFF:
            case TEXT:
                return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName()) + "())";
            case TIME:
                TimeItemDef timeItem = (TimeItemDef) item;
                switch (timeItem.getFormat()) {
                    case DATE:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                                + "() == null ? null : LocalDateTime.of(req.get" + StringUtils.capitalize(
                                item.getName()) + "(), LocalTime.of(0, 0)))";
                    case TIME:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                                + "() == null ? null : LocalDateTime.of(LocalDate.of(1970, 1, 1), req.get"
                                + StringUtils.capitalize(item.getName()) + "()))";
                    case DATE_TIME:
                        return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName()) + "())";
                    default:
                        throw new RuntimeException("impossible");
                }
            case SELECT:
                return "." + item.getName() + ".eq(req.get" + StringUtils.capitalize(item.getName())
                        + "() == null ? null : req.get" + StringUtils.capitalize(item.getName()) + "().getCode())";
            default:
                throw new RuntimeException("impossible");
        }
    }

}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/MutationApiSupport.java form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MutationApiSupportImpl.java
git commit -m "refactor: extract MutationApiSupport with shared create/update logic"
```

---

### Task 2: Create CreateApiService interface and implementation

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/CreateApiService.java`
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/CreateApiServiceImpl.java`

- [ ] **Step 1: Create CreateApiService interface**

```java
package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.CreateApiServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(CreateApiServiceImpl.class)
public interface CreateApiService {

    InitializerDeclaration generateCreateInitDec(FormDef formDef);

    BlockStmt generateCreateMethodBody(FormDef form);

}
```

- [ ] **Step 2: Create CreateApiServiceImpl implementation**

```java
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
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.CreateApiService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.MutationApiSupport;
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
    private MutationApiSupport mutationApiSupport;

    @Override
    public InitializerDeclaration generateCreateInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement("String handler = \"create%s\", desc = \"创建%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                ApiType.CREATE.getCode()));

        // req declaration — only canInputOnInit=true fields, no bizId
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        for (ItemDef item : form.getItems()) {
            if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp declaration — contains bizId
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

        // new entity + set bizId
        body.addStatement(parseStatement("%s %s = new %s();", form.getEntityName(config), form.getVarName(),
                form.getEntityName(config)));
        body.addStatement(parseStatement("%s.%s(%s);", form.getVarName(), form.getBizIdSetterName(),
                config.getCodeSnippet().getShortUuidGeneration()));

        // set fields with canInputOnInit=true
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                mutationApiSupport.generateSetterToGetter(form, item, body);
            } else if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                // canInputOnInit=false but nonVoid → set default value
                body.addStatement(
                        parseStatement("%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                                itemService.getTodoValue(item)));
            }
        }

        // set createdAt
        body.addStatement(parseStatement("%s.setCreatedAt(LocalDateTime.now());", form.getVarName()));

        // unique-index existence check for init-applicable indices
        if (form.getIndices() != null) {
            for (IndexDef index : form.getIndices()) {
                if (Boolean.TRUE.equals(index.getIsUnique()) && mutationApiSupport.allCanInput(form, index, true)) {
                    mutationApiSupport.generateCheckExistStatement(form, index).forEach(body::addStatement);
                }
            }
        }

        // set updatedAt
        mutationApiSupport.generateSetUpdatedAt(form, body);

        // insert
        body.addStatement(parseStatement("%sMapper.insert(%s);", form.getVarName(), form.getVarName()));

        // multiSelect association
        mutationApiSupport.generateMultiSelectAssociation(form, body);

        // return resp with bizId
        body.addStatement(parseStatement(
                "return new Create" + form.getName() + "Resp()." + form.getBizIdSetterName() + "(" + form.getVarName()
                        + "." + form.getBizIdGetterName() + "());"));
        return body;
    }

}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/CreateApiService.java form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/CreateApiServiceImpl.java
git commit -m "feat: add CreateApiService for independent create API generation"
```

---

### Task 3: Create UpdateApiService interface and implementation

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/UpdateApiService.java`
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java`

- [ ] **Step 1: Create UpdateApiService interface**

```java
package com.spldeolin.allison1875.formgenerator.service;

import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.UpdateApiServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(UpdateApiServiceImpl.class)
public interface UpdateApiService {

    InitializerDeclaration generateUpdateInitDec(FormDef formDef);

    BlockStmt generateUpdateMethodBody(FormDef form);

}
```

- [ ] **Step 2: Create UpdateApiServiceImpl implementation**

```java
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
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.MutationApiSupport;
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
    private MutationApiSupport mutationApiSupport;

    @Override
    public InitializerDeclaration generateUpdateInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement("String handler = \"update%s\", desc = \"更新%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                ApiType.UPDATE.getCode()));

        // req declaration — bizId + canInputOnEdit=true fields
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = parseFieldDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;");
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // no resp (void return)
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp");
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));

        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateUpdateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();

        // query by bizId + not-found check
        body.addStatement(parseStatement("%s %s = %sMapper.queryBy%s(req.%s());", form.getEntityName(config),
                form.getVarName(), form.getVarName(), StringUtils.capitalize(form.getBizIdName()),
                form.getBizIdGetterName()));
        body.addStatement(
                parseStatement("if (%s == null) { throw new %s(\"%s不存在或是已被删除\"); }", form.getVarName(),
                        config.getCodeSnippet().getBizExceptionQualifier(), form.getTitle()));

        // set fields with canInputOnEdit=true
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                mutationApiSupport.generateSetterToGetter(form, item, body);
            }
        }

        // unique-index existence check for edit-applicable indices
        if (form.getIndices() != null) {
            for (IndexDef index : form.getIndices()) {
                if (Boolean.TRUE.equals(index.getIsUnique()) && mutationApiSupport.allCanInput(form, index, false)) {
                    mutationApiSupport.generateCheckExistStatement(form, index).forEach(body::addStatement);
                }
            }
        }

        // set updatedAt
        mutationApiSupport.generateSetUpdatedAt(form, body);

        // updateById
        body.addStatement(parseStatement("%sMapper.updateById(%s);", form.getVarName(), form.getVarName()));

        // multiSelect association
        mutationApiSupport.generateMultiSelectAssociation(form, body);

        return body;
    }

}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/UpdateApiService.java form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java
git commit -m "feat: add UpdateApiService for independent update API generation"
```

---

### Task 4: Update ApiType enum

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ApiType.java`

- [ ] **Step 1: Replace SAVE with CREATE and UPDATE**

Replace the entire enum body. Change `SAVE("save")` to `CREATE("create")` and add `UPDATE("update")`:

```java
package com.spldeolin.allison1875.formgenerator.dsl.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2026-02-15
 */
@Getter
@AllArgsConstructor
public enum ApiType {

    CREATE("create"),

    UPDATE("update"),

    LIST("list"),

    GET_DETAIL("getDetail"),

    DELETE("delete"),

    ;

    @JsonValue
    private final String code;

    @JsonCreator
    public static ApiType of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: FAILURE (references to `ApiType.SAVE` in other files still exist — will be fixed in next tasks)

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ApiType.java
git commit -m "refactor: replace ApiType.SAVE with CREATE and UPDATE"
```

---

### Task 5: Update FormGenerator to use CreateApiService and UpdateApiService

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java`

- [ ] **Step 1: Replace SaveApiService injection with CreateApiService + UpdateApiService**

In `FormGenerator.java`, replace the `SaveApiService` field (lines 87-88):

Old:
```java
    @Inject
    private SaveApiService saveApiService;
```

New:
```java
    @Inject
    private CreateApiService createApiService;

    @Inject
    private UpdateApiService updateApiService;
```

- [ ] **Step 2: Update the import statements**

Replace:
```java
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
```

With:
```java
import com.spldeolin.allison1875.formgenerator.service.CreateApiService;
import com.spldeolin.allison1875.formgenerator.service.UpdateApiService;
```

- [ ] **Step 3: Update the controller generation to add both initDecs**

Replace line 149:
```java
            coid.addMember(saveApiService.generateSaveInitDec(form));
```

With:
```java
            coid.addMember(createApiService.generateCreateInitDec(form));
            coid.addMember(updateApiService.generateUpdateInitDec(form));
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: FAILURE (FormGeneratorServiceLayerExpansionServiceImpl still references SaveApiService — fixed next)

- [ ] **Step 5: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java
git commit -m "refactor: use CreateApiService and UpdateApiService in FormGenerator"
```

---

### Task 6: Update FormGeneratorServiceLayerExpansionServiceImpl

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorServiceLayerExpansionServiceImpl.java`

- [ ] **Step 1: Replace SaveApiService injection with CreateApiService + UpdateApiService**

Replace the import and field:

Old (lines 4-5):
```java
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.SAVE;
```

New:
```java
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.CREATE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.UPDATE;
```

Replace the field (lines 57-58):
```java
    @Inject
    private SaveApiService saveApiService;
```

With:
```java
    @Inject
    private CreateApiService createApiService;

    @Inject
    private UpdateApiService updateApiService;
```

Replace import:
```java
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
```

With:
```java
import com.spldeolin.allison1875.formgenerator.service.CreateApiService;
import com.spldeolin.allison1875.formgenerator.service.UpdateApiService;
```

- [ ] **Step 2: Update buildAnnotationsFormServiceImplMethod**

Replace line 65:
```java
        if (Lists.newArrayList(SAVE, DELETE).contains(ApiType.of(initDecAnalysis.getExpansion().get("type")))) {
```

With:
```java
        if (Lists.newArrayList(CREATE, UPDATE, DELETE).contains(ApiType.of(initDecAnalysis.getExpansion().get("type")))) {
```

- [ ] **Step 3: Update buildServiceImplMethodBody switch**

Replace the SAVE case (lines 80-82):
```java
            case SAVE:
                body = saveApiService.generateMethodBody(form);
                break;
```

With:
```java
            case CREATE:
                body = createApiService.generateCreateMethodBody(form);
                break;
            case UPDATE:
                body = updateApiService.generateUpdateMethodBody(form);
                break;
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorServiceLayerExpansionServiceImpl.java
git commit -m "refactor: route create/update types to respective services in expansion"
```

---

### Task 7: Delete SaveApiService and SaveApiServiceImpl

**Files:**
- Delete: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/SaveApiService.java`
- Delete: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SaveApiServiceImpl.java`

- [ ] **Step 1: Delete the files**

```bash
rm form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/SaveApiService.java
rm form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SaveApiServiceImpl.java
```

- [ ] **Step 2: Verify compilation of the entire project**

Run: `mvn compile -q`
Expected: BUILD SUCCESS (no remaining references to SaveApiService)

- [ ] **Step 3: Commit**

```bash
git add -u
git commit -m "refactor: remove SaveApiService — fully replaced by Create + Update"
```

---

### Task 8: Update ControllerAuthAnnotateServiceImpl

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/ControllerAuthAnnotateServiceImpl.java`

- [ ] **Step 1: Replace save matching with create + update matching**

Replace the `@PostMapping("save")` matching block (line 67-70):

Old:
```java
            if (line.contains("@PostMapping(\"save")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth({PermissionEnum.CREATE_" + upperSnake + ", PermissionEnum.UPDATE_"
                        + upperSnake + "})");
            } else if (line.contains("@PostMapping(\"list")) {
```

New:
```java
            if (line.contains("@PostMapping(\"create")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.CREATE_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"update")) {
                String indent = extractIndent(line);
                result.add(indent + "@WebApiAuth(PermissionEnum.UPDATE_" + upperSnake + ")");
            } else if (line.contains("@PostMapping(\"list")) {
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl app-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/ControllerAuthAnnotateServiceImpl.java
git commit -m "refactor: annotate create/update endpoints with individual permissions"
```

---

### Task 9: Update frontend skeleton — endpoints.ts

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/endpoints.ts`

- [ ] **Step 1: Replace save with create + update in CrudAction and endpointOf**

Full replacement of the file:

```typescript
// protocol/endpoints.ts
// URL derivation rules — sourced from contract.md §1-2
// All endpoints use POST method.
//
// URL pattern:
//   base  = /api/v1/${lowerCamelFormName}
//   list      = base/list${pluralize(FormName)}
//   create    = base/create${FormName}
//   update    = base/update${FormName}
//   getDetail = base/get${FormName}Detail
//   delete    = base/delete${FormName}

import { upperCamelToLowerCamel, pluralize } from '@/utils/naming'

export type CrudAction = 'list' | 'create' | 'update' | 'delete' | 'getDetail'

export function endpointOf(formName: string, action: CrudAction): string {
  const lower = upperCamelToLowerCamel(formName)
  const base = `/api/v1/${lower}`
  switch (action) {
    case 'list':
      return `${base}/list${pluralize(formName)}`
    case 'create':
      return `${base}/create${formName}`
    case 'update':
      return `${base}/update${formName}`
    case 'getDetail':
      return `${base}/get${formName}Detail`
    case 'delete':
      return `${base}/delete${formName}`
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/endpoints.ts
git commit -m "refactor: split save endpoint into create + update in frontend skeleton"
```

---

### Task 10: Update frontend skeleton — request-builder.ts

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts`

- [ ] **Step 1: Replace buildSaveRequest with buildCreateRequest + buildUpdateRequest**

Replace lines 80-96 (the `buildSaveRequest` function) with:

```typescript

export function buildCreateRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    if (item.canInputOnInit === false) continue
    out[item.name] = formState[item.name] ?? null
  }
  return out
}

export function buildUpdateRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
  bizKey: string,
): Record<string, unknown> {
  const out: Record<string, unknown> = { [bizKey]: formState[bizKey] }
  for (const item of items) {
    if (item.canInputOnEdit === false) continue
    out[item.name] = formState[item.name] ?? null
  }
  return out
}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts
git commit -m "refactor: split buildSaveRequest into buildCreateRequest + buildUpdateRequest"
```

---

### Task 11: Update frontend skeleton — useCrudPage.ts

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/composables/useCrudPage.ts`

- [ ] **Step 1: Update import to use new request builder functions**

Replace line 7:
```typescript
import { buildListRequest, buildSaveRequest } from '../protocol/request-builder'
```

With:
```typescript
import { buildListRequest, buildCreateRequest, buildUpdateRequest } from '../protocol/request-builder'
```

- [ ] **Step 2: Update handleSubmit to call separate endpoints**

Replace the `handleSubmit` function (lines 139-156):

```typescript
  async function handleSubmit() {
    const schema = getSchema()
    submitLoading.value = true
    try {
      let reqBody: Record<string, unknown>
      let endpoint: string
      if (modalMode.value === 'create') {
        reqBody = buildCreateRequest(schema.items, formData.value)
        endpoint = endpointOf(schema.name, 'create')
      } else {
        reqBody = buildUpdateRequest(schema.items, formData.value, bizKey.value)
        endpoint = endpointOf(schema.name, 'update')
      }
      await request.post(endpoint, reqBody)
      message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
      modalVisible.value = false
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '保存失败')
    } finally {
      submitLoading.value = false
    }
  }
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/composables/useCrudPage.ts
git commit -m "refactor: call create/update endpoints separately in handleSubmit"
```

---

### Task 12: Update IT test — replace SaveApiItTest with CreateUpdateApiItTest

**Files:**
- Modify: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SaveApiItTest.java` → rename to `CreateUpdateApiItTest.java`
- Modify: `allison1875-cli/src/test/resources/it/form-generator/save-api/` → rename to `create-update-api/`

- [ ] **Step 1: Rename test resource directory**

```bash
mv allison1875-cli/src/test/resources/it/form-generator/save-api allison1875-cli/src/test/resources/it/form-generator/create-update-api
```

- [ ] **Step 2: Create the new test class**

Delete the old test file and create `CreateUpdateApiItTest.java`:

```java
package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * create/update API 集成测试。
 *
 * <p>验证 Save API 拆分为 Create + Update 后的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code createItem} 和 {@code updateItem} 两个 handler</li>
 *   <li>CreateReq 只含 canInputOnInit=true 字段（无 bizId），CreateResp 含 bizId</li>
 *   <li>UpdateReq 含 bizId + canInputOnEdit=true 字段，无 Resp</li>
 *   <li>CreateServiceImpl：new Entity → setBizId → setCreatedAt → setUpdatedAt → insert</li>
 *   <li>UpdateServiceImpl：queryByBizId → null check → setters → setUpdatedAt → updateById</li>
 * </ul>
 *
 * @author Deolin 2026-06-23
 */
public class CreateUpdateApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("create-update-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ItemController.java");
        assertTrue(controllerFile.exists(), "ItemController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("createItem"), "Controller should contain createItem handler");
        assertTrue(controllerContent.contains("updateItem"), "Controller should contain updateItem handler");
        assertFalse(controllerContent.contains("saveItem"), "Controller should NOT contain saveItem handler");

        // ============================================================
        // === CreateReq DTO 验证 ===
        // ============================================================
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateItemReq.java");
        assertTrue(createReqFile.exists(), "CreateItemReq DTO should be generated");
        String createReqContent = Files.readString(createReqFile.toPath());
        assertTrue(createReqContent.contains("class CreateItemReq"));
        // No bizId in create request
        assertFalse(createReqContent.contains("itemCode"), "CreateReq should NOT contain itemCode");
        // User fields
        assertTrue(createReqContent.contains("itemName"), "CreateReq should contain itemName");
        assertTrue(createReqContent.contains("quantity"), "CreateReq should contain quantity");
        // Validation annotations directly on fields
        assertTrue(createReqContent.contains("@NotBlank"), "nonVoid text should have @NotBlank");
        assertTrue(createReqContent.contains("@NotNull"), "nonVoid number/onOff should have @NotNull");

        // ============================================================
        // === CreateResp DTO 验证 ===
        // ============================================================
        File createRespFile = new File(basedir, "src/main/java/com/example/dto/resp/CreateItemResp.java");
        assertTrue(createRespFile.exists(), "CreateItemResp DTO should be generated");
        String createRespContent = Files.readString(createRespFile.toPath());
        assertTrue(createRespContent.contains("class CreateItemResp"));
        assertTrue(createRespContent.contains("itemCode"), "CreateResp should contain itemCode (return bizId)");

        // ============================================================
        // === UpdateReq DTO 验证 ===
        // ============================================================
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateItemReq.java");
        assertTrue(updateReqFile.exists(), "UpdateItemReq DTO should be generated");
        String updateReqContent = Files.readString(updateReqFile.toPath());
        assertTrue(updateReqContent.contains("class UpdateItemReq"));
        // bizId present in update request
        assertTrue(updateReqContent.contains("itemCode"), "UpdateReq should contain itemCode as bizId");
        // User fields
        assertTrue(updateReqContent.contains("itemName"), "UpdateReq should contain itemName");

        // ============================================================
        // === Create ServiceImpl 验证 ===
        // ============================================================
        File createServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateItemServiceImpl.java");
        assertTrue(createServiceImplFile.exists(), "CreateItemServiceImpl should be generated");
        String createContent = Files.readString(createServiceImplFile.toPath());
        assertTrue(createContent.contains("class CreateItemServiceImpl"));
        assertTrue(createContent.contains("new ItemEntity()"), "Create should instantiate new entity");
        assertTrue(createContent.contains("UUID.randomUUID()"), "Create should generate UUID for bizId");
        assertTrue(createContent.contains("setCreatedAt("), "Create should set createdAt");
        assertTrue(createContent.contains("setUpdatedAt("), "Create should set updatedAt");
        assertTrue(createContent.contains("itemMapper.insert("), "Create should call insert");
        assertFalse(createContent.contains("toCreate"), "Create should NOT have toCreate branch logic");
        assertTrue(createContent.contains("new CreateItemResp()"), "Create should return CreateItemResp");

        // ============================================================
        // === Update ServiceImpl 验证 ===
        // ============================================================
        File updateServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateItemServiceImpl.java");
        assertTrue(updateServiceImplFile.exists(), "UpdateItemServiceImpl should be generated");
        String updateContent = Files.readString(updateServiceImplFile.toPath());
        assertTrue(updateContent.contains("class UpdateItemServiceImpl"));
        assertTrue(updateContent.contains("itemMapper.queryByItemCode("), "Update should query by bizId");
        assertTrue(updateContent.contains("RuntimeException"), "Update should throw if not found");
        assertTrue(updateContent.contains("setUpdatedAt("), "Update should set updatedAt");
        assertTrue(updateContent.contains("itemMapper.updateById("), "Update should call updateById");
        assertFalse(updateContent.contains("toCreate"), "Update should NOT have toCreate branch logic");
        assertFalse(updateContent.contains("new ItemEntity()"), "Update should NOT create new entity");

        // ============================================================
        // === 验证不存在 Save 相关文件 ===
        // ============================================================
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveItemReq.java");
        assertFalse(saveReqFile.exists(), "SaveItemReq should NOT exist");
        File saveServiceFile = new File(basedir, "src/main/java/com/example/service/SaveItemService.java");
        assertFalse(saveServiceFile.exists(), "SaveItemService should NOT exist");
    }

}
```

- [ ] **Step 3: Delete old test file**

```bash
rm allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SaveApiItTest.java
```

- [ ] **Step 4: Run the new IT test**

Run: `mvn test -pl allison1875-cli -am -Dtest=CreateUpdateApiItTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "test: replace SaveApiItTest with CreateUpdateApiItTest for split APIs"
```

---

### Task 13: Run full test suite and fix any remaining issues

**Files:**
- Potentially modify: any file with remaining references to `save` API pattern

- [ ] **Step 1: Run the full test suite**

Run: `mvn verify -q`
Expected: BUILD SUCCESS with all tests passing

- [ ] **Step 2: If BasicFormItTest or other tests fail, update their assertions**

Any test that previously asserted `saveItem`/`SaveItemReq`/`SaveItemResp` needs to assert `createItem`/`updateItem`/`CreateItemReq`/`UpdateItemReq` instead. Check `BasicFormItTest.java` specifically — it likely verifies controller handler names.

- [ ] **Step 3: Commit any fixes**

```bash
git add -A
git commit -m "test: fix remaining IT assertions after save→create/update split"
```

---

### Task 14: Update CLAUDE.md documentation

**Files:**
- Modify: `form-generator/CLAUDE.md` (update canInputOnInit/canInputOnEdit table if needed)

- [ ] **Step 1: Update the canInputOnInit/canInputOnEdit combination semantics table**

In `form-generator/CLAUDE.md`, replace the table under "### canInputOnInit / canInputOnEdit 组合语义":

Old:
```markdown
| init  | edit  | ReqDTO 字段 |          校验位置           |      setter 位置       |
|:-----:|:-----:|:---------:|:-----------------------:|:--------------------:|
| true  | true  |     有     |        ReqDTO 注解        | common 节（if/else 之外） |
| true  | false |     有     | if(toCreate) 内 if-throw |    if(toCreate) 内    |
| false | true  |     有     |     else 内 if-throw     |        else 内        |
| false | false |     无     |            —            |  if(toCreate) 内写默认值  |
```

New:
```markdown
| init  | edit  | CreateReq 字段 | UpdateReq 字段 |       校验位置        |
|:-----:|:-----:|:-----------:|:-----------:|:-----------------:|
| true  | true  |      有      |      有      | 各自 ReqDTO 注解均带校验 |
| true  | false |      有      |      无      |  CreateReq 注解带校验  |
| false | true  |      无      |      有      |  UpdateReq 注解带校验  |
| false | false |      无      |      无      |    Create 内写默认值    |
```

- [ ] **Step 2: Commit**

```bash
git add form-generator/CLAUDE.md
git commit -m "docs: update CLAUDE.md for create/update API split"
```
