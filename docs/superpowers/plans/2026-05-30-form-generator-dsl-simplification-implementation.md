# form-generator DSL 简化 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `InitOrEditPattern` enum with two boolean fields `canInputOnInit`/`canInputOnEdit` on `ItemDef`; reshape `SaveApiServiceImpl` for 4 boolean combinations; sync IT cases, spec docs, super-dsl.

**Architecture:** Single-module change in `form-generator/`; minimal ripple to `allison1875-cli/` IT cases and `docs/superpowers/specs/`. `SaveApiServiceImpl` rewriting keeps the three-section structure (`if(toCreate)` / `else` / common) and adds an `ItemService.getValidationStatement(item)` abstract method to emit `if(empty) throw` blocks for branch-local non-void validation.

**Tech Stack:** Java 21, JavaParser, Guice, Jackson, jakarta.validation, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-05-30-form-generator-dsl-simplification-design.md`

---

## File Structure

**form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/**
- `dsl/ItemDef.java` — modify: drop `initPattern`/`editPattern` enums, add `canInputOnInit`/`canInputOnEdit` booleans (default `true`)
- `dsl/enums/InitOrEditPattern.java` — delete
- `FormGenerator.java` — modify `addCommonItems`: bizId/createdAt/updatedAt → `(false,false)`
- `service/ItemService.java` — add abstract `getValidationStatement(I)`
- `service/impl/PrimaryItemServiceImpl.java` — delegate `getValidationStatement`
- `service/impl/{Number,OnOff,Secret,Select,Text,Time,MultiSelect}ItemService.java` — each implements `getValidationStatement`
- `service/impl/SaveApiServiceImpl.java` — rewrite per 4-case table; remove `// TODO 请补充...` comments
- `service/impl/MultiSelectItemService.java` — replace `setInitPattern(DO_NOT)`/`setEditPattern(DO_NOT)` with boolean setters

**allison1875-cli/src/test/resources/it/form-generator/**
- delete: `init-pattern-todo/`, `edit-pattern-donot/`, `mixed-init-edit-pattern/`
- create: `cannot-input-on-edit/{forms.yml,pom.xml,src/}`
- create: `cannot-input-on-init/{forms.yml,pom.xml,src/}`

**allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/**
- delete: `InitPatternTodoItTest.java`, `EditPatternDoNotItTest.java`, `MixedInitEditPatternItTest.java`
- create: `CannotInputOnEditItTest.java`, `CannotInputOnInitItTest.java`
- modify: `SaveApiItTest.java`, `CommonItemsAutoAddItTest.java`, `README.md`

**docs/superpowers/specs/** (rename-only edits)
- `2026-05-23-form-web-design.md`
- `2026-05-24-app-generator-design.md`
- `2026-05-28-aligning-frontend-with-form-generator-design.md`
- `2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md`
- `2026-05-28-aligning-frontend-with-form-generator/gaps.md`
- `2026-05-28-aligning-frontend-with-form-generator/contract.md`
- `2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`

**skills/** (rename-only edits)
- `skills/generate-form/SKILL.md`
- `skills/integrate-allison1875/examples/forms.yml`

---

## Task 1: Add boolean fields to `ItemDef` (keep enum temporarily)

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`

This task adds the new boolean fields **without deleting the enum yet**, so the project still compiles. Subsequent tasks migrate callers, and Task 11 deletes the enum.

- [ ] **Step 1: Add `canInputOnInit` and `canInputOnEdit` fields**

Edit `ItemDef.java` — keep the existing `initPattern`/`editPattern` fields untouched. Append:

```java
    /**
     * 创建（init）时是否允许用户输入。默认 true。
     */
    @NotNull
    Boolean canInputOnInit = true;

    /**
     * 编辑（edit）时是否允许用户输入。默认 true。
     */
    @NotNull
    Boolean canInputOnEdit = true;
```

(Place these immediately after the existing `editPattern` field, before `getType()`.)

- [ ] **Step 2: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java
git commit -m "feat(form-generator): add canInputOnInit/canInputOnEdit boolean fields to ItemDef"
```

---

## Task 2: Add `getValidationStatement` to `ItemService` interface

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/ItemService.java`

- [ ] **Step 1: Add abstract method to ItemService**

In `ItemService.java`, add after `getTodoValue`:

```java
    /**
     * 生成"如果字段为空则抛出异常"的校验语句，用于条件分支内的延迟校验。
     * 示例：if (req.getXxx() == null) { throw new IllegalArgumentException("xxx不能为空"); }
     *
     * 仅在 isNonVoid==true 且字段处于 (canInputOnInit=true,canInputOnEdit=false) 或
     * (canInputOnInit=false,canInputOnEdit=true) 组合时调用。
     */
    com.github.javaparser.ast.stmt.Statement getValidationStatement(I itemDef);
```

- [ ] **Step 2: Compile (will fail with abstract method not implemented)**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: COMPILATION ERROR — each `*ItemService.java` (Number, OnOff, Secret, Select, Text, Time, MultiSelect) and `PrimaryItemServiceImpl` is missing `getValidationStatement`.

- [ ] **Step 3: Do not commit yet** — wait until Task 3 implements all method bodies.

---

## Task 3: Implement `getValidationStatement` in all `ItemService` impls

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/TextItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SecretItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/NumberItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/OnOffItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SelectItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/TimeItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MultiSelectItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java`

`Statement getValidationStatement` returns a `com.github.javaparser.ast.stmt.Statement` parsed from a string literal using `parseStatement(...)`. The error message format: `"创建XX时YY不能为空"` is hard-coded in spec; we use the simpler `"YY不能为空"` to match item title (form title isn't reachable from item alone).

- [ ] **Step 1: Implement in `TextItemService.java`**

Add import:
```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

Append method:
```java
    @Override
    public Statement getValidationStatement(TextItemDef itemDef) {
        return parseStatement(
                "if (org.apache.commons.lang3.StringUtils.isBlank(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 2: Implement in `SecretItemService.java`**

Same imports and method as TextItemService (only the type parameter differs — `SecretItemDef`).

```java
    @Override
    public Statement getValidationStatement(SecretItemDef itemDef) {
        return parseStatement(
                "if (org.apache.commons.lang3.StringUtils.isBlank(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 3: Implement in `NumberItemService.java`**

Add imports:
```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

Append method:
```java
    @Override
    public Statement getValidationStatement(NumberItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 4: Implement in `OnOffItemService.java`**

Same imports as NumberItemService; same method body shape (`itemDef` type is `OnOffItemDef`).

```java
    @Override
    public Statement getValidationStatement(OnOffItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 5: Implement in `SelectItemService.java`**

Same as NumberItemService (parameter type `SelectItemDef`).

```java
    @Override
    public Statement getValidationStatement(SelectItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 6: Implement in `TimeItemService.java`**

Same as NumberItemService (parameter type `TimeItemDef`).

```java
    @Override
    public Statement getValidationStatement(TimeItemDef itemDef) {
        return parseStatement(
                "if (req.get%s() == null) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

- [ ] **Step 7: Implement in `MultiSelectItemService.java`**

Add imports (StringUtils & Statement & parseStatement) — `StringUtils` already imported in this file.

```java
    @Override
    public Statement getValidationStatement(MultiSelectItemDef itemDef) {
        return parseStatement(
                "if (org.springframework.util.CollectionUtils.isEmpty(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }
```

Add `import com.github.javaparser.ast.stmt.Statement;` and the static import for `parseStatement`.

- [ ] **Step 8: Implement in `PrimaryItemServiceImpl.java` (delegating)**

Add import: `import com.github.javaparser.ast.stmt.Statement;`

Append method:
```java
    @Override
    public Statement getValidationStatement(ItemDef itemDef) {
        return delegate(itemDef).getValidationStatement(itemDef);
    }
```

- [ ] **Step 9: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 10: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/ItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/TextItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SecretItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/NumberItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/OnOffItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SelectItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/TimeItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MultiSelectItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java
git commit -m "feat(form-generator): add ItemService.getValidationStatement abstract method and implementations"
```

---

## Task 4: Migrate `FormGenerator.addCommonItems` to boolean fields

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java`

- [ ] **Step 1: Replace pattern setters in addCommonItems**

In `FormGenerator.java`, lines ~185-205. Replace:

```java
            bizId.setInitPattern(TODO);
            bizId.setEditPattern(InitOrEditPattern.DO_NOT);
```
with:
```java
            bizId.setCanInputOnInit(false);
            bizId.setCanInputOnEdit(false);
```

Replace:
```java
            createdAt.setInitPattern(TODO);
            createdAt.setEditPattern(InitOrEditPattern.DO_NOT);
```
with:
```java
            createdAt.setCanInputOnInit(false);
            createdAt.setCanInputOnEdit(false);
```

Replace:
```java
            updatedAt.setInitPattern(TODO);
            updatedAt.setEditPattern(TODO);
```
with:
```java
            updatedAt.setCanInputOnInit(false);
            updatedAt.setCanInputOnEdit(false);
```

- [ ] **Step 2: Remove the unused imports**

Delete these two lines from the top of `FormGenerator.java`:
```java
import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.TODO;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
```

- [ ] **Step 3: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java
git commit -m "refactor(form-generator): migrate addCommonItems to boolean canInputOn* fields"
```

---

## Task 5: Migrate `MultiSelectItemService.toAssociationForm` to boolean fields

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MultiSelectItemService.java`

- [ ] **Step 1: Replace pattern setters in toAssociationForm**

In `toAssociationForm`, replace:
```java
        code.setInitPattern(DO_NOT); // 非主表单，init和edit没有意义
        code.setEditPattern(DO_NOT);
```
with:
```java
        code.setCanInputOnInit(false); // 非主表单，init和edit没有意义
        code.setCanInputOnEdit(false);
```

Replace:
```java
        createdAt.setInitPattern(DO_NOT);
        createdAt.setEditPattern(DO_NOT);
```
with:
```java
        createdAt.setCanInputOnInit(false);
        createdAt.setCanInputOnEdit(false);
```

- [ ] **Step 2: Remove the unused import**

Delete:
```java
import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.DO_NOT;
```

- [ ] **Step 3: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/MultiSelectItemService.java
git commit -m "refactor(form-generator): migrate toAssociationForm to boolean canInputOn* fields"
```

---

## Task 6: Rewrite `SaveApiServiceImpl` for 4-case boolean logic

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SaveApiServiceImpl.java`

This is the central rewrite. Replace the file's body with the new logic.

- [ ] **Step 1: Replace the file content**

Overwrite `SaveApiServiceImpl.java` with:

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseExpression;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseVariableDeclarationExpr;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class SaveApiServiceImpl implements SaveApiService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private Config config;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public InitializerDeclaration generateSaveInitDec(FormDef form) {
        BlockStmt bs = new BlockStmt();
        bs.addStatement(parseStatement(
                "String handler = \"save%s\", desc = \"创建%s\", form=\"%s\", type=\"%s\";",
                form.getName(), form.getTitle(), StringEscapeUtils.escapeJava(JsonUtils.toJson(form)),
                ApiType.SAVE.getCode()));

        // req declaration
        ClassOrInterfaceDeclaration reqCoid = new ClassOrInterfaceDeclaration().setName("req");
        FieldDeclaration bizIdField = parseFieldDeclaration(
                "String " + StringUtils.uncapitalize(form.getName()) + "Code;");
        JavadocUtils.setJavadoc(bizIdField, form.getTitle() + "的业务ID", null);
        reqCoid.addMember(bizIdField);
        for (ItemDef item : form.getItems()) {
            // 字段进入 ReqDTO 当且仅当 init 或 edit 任一允许用户输入
            if (Boolean.TRUE.equals(item.getCanInputOnInit()) || Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                // 仅在 (true,true) 组合时把 isNonVoid 校验注解放在 ReqDTO 字段上；
                // 其他组合的 isNonVoid 校验改为分支内 if-throw（见 generateMethodBody/IfThen/Else）。
                if (Boolean.TRUE.equals(item.getCanInputOnInit())
                        && Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                    itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                }
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
        bs.addStatement(new LocalClassDeclarationStmt(reqCoid));

        // resp declaration
        ClassOrInterfaceDeclaration respCoid = new ClassOrInterfaceDeclaration().setName("resp").addMember(bizIdField);
        bs.addStatement(new LocalClassDeclarationStmt(respCoid));
        return new InitializerDeclaration(false, bs);
    }

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "boolean toCreate = req.%s() == null;", form.getBizIdGetterName()));
        body.addStatement(parseStatement(
                "%s %s;", form.getEntityName(config), form.getVarName()));
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(new NameExpr("toCreate"));
        ifStmt.setThenStmt(generateIfThenBody(form));
        ifStmt.setElseStmt(generateElseBody(form));
        body.addStatement(ifStmt);

        // common section: only (canInputOnInit=true, canInputOnEdit=true) 字段在此设置
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnInit())
                    && Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                generatorSetterToGetter(form, item, body);
            }
        }
        body.addStatement(parseStatement(
                "%s.setUpdatedAt(LocalDateTime.now());", form.getVarName()));
        body.addStatement(parseStatement(
                "if (toCreate) { %sMapper.insert(%s); } else { %sMapper.updateById(%s); }",
                form.getVarName(), form.getVarName(), form.getVarName(), form.getVarName()));

        // 删除、重新创建关联实体（multiSelect 路径不变）
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                Statement stmt = parseStatement(
                        "%sMapper.deleteBy%s(%s.%s());", associationForm.getVarName(),
                        StringUtils.capitalize(associationForm.getBizIdName()), form.getVarName(),
                        associationForm.getBizIdGetterName());
                stmt.setLineComment(String.format("重建与%s的关联（先删除，后创建）", item.getTitle()));
                body.addStatement(stmt);
                ForEachStmt forEachStmt = new ForEachStmt();
                forEachStmt.setVariable(parseVariableDeclarationExpr(
                        String.format("%s %s", MoreStringUtils.toUpperCamel(item.getName()) + "Enum", item.getName())));
                forEachStmt.setIterable(parseExpression(
                        String.format("req.get%s()", StringUtils.capitalize(item.getName()))));
                BlockStmt forEachBody = new BlockStmt();
                forEachBody.addStatement(parseStatement("%s %s = new %s();", associationForm.getEntityName(config),
                        associationForm.getVarName(), associationForm.getEntityName(config)));
                forEachBody.addStatement(parseStatement(
                        "%s.%s(%s.%s());", associationForm.getVarName(),
                        associationForm.getBizIdSetterName(), form.getVarName(), form.getBizIdGetterName()));
                forEachBody.addStatement(parseStatement(
                        "%s.set%s(%s.getCode());", associationForm.getVarName(),
                        StringUtils.capitalize(item.getName()), item.getName()));
                forEachBody.addStatement(parseStatement(
                        "%s.setCreatedAt(LocalDateTime.now());", associationForm.getVarName()));
                forEachBody.addStatement(parseStatement(
                        "%sMapper.insert(%s);", associationForm.getVarName(),
                        associationForm.getVarName()));
                forEachStmt.setBody(forEachBody);
                body.addStatement(forEachStmt);
            }
        }

        body.addStatement(parseStatement(
                "return new Save" + form.getName() + "Resp()." + form.getBizIdSetterName() + "(" + form.getVarName()
                        + "." + form.getBizIdGetterName() + "());"));
        return body;
    }

    private Statement generateIfThenBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "%s = new %s();", form.getVarName(), form.getEntityName(config)));
        body.addStatement(parseStatement(
                "%s.%s(%s);", form.getVarName(), form.getBizIdSetterName(),
                config.getCodeSnippet().getShortUuidGeneration()));
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            boolean init = Boolean.TRUE.equals(item.getCanInputOnInit());
            boolean edit = Boolean.TRUE.equals(item.getCanInputOnEdit());

            if (init && !edit) {
                // case (true,false): if isNonVoid → if-throw, then setter
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                generatorSetterToGetter(form, item, body);
            } else if (!init) {
                // case (false,true) and (false,false): non-void → default value
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(parseStatement(
                            "%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                            itemService.getTodoValue(item)));
                }
            }
        }
        body.addStatement(parseStatement(
                "%s.setCreatedAt(LocalDateTime.now());", form.getVarName()));
        return body;
    }

    private Statement generateElseBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(parseStatement(
                "%s = %sMapper.queryBy%s(req.%s());", form.getVarName(), form.getVarName(),
                StringUtils.capitalize(form.getBizIdName()), form.getBizIdGetterName()));
        body.addStatement(parseStatement(
                "if (%s == null) { throw new RuntimeException(\"%s不存在或是已被删除\"); }",
                form.getVarName(), form.getTitle()));
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            boolean init = Boolean.TRUE.equals(item.getCanInputOnInit());
            boolean edit = Boolean.TRUE.equals(item.getCanInputOnEdit());
            if (!init && edit) {
                // case (false,true): if isNonVoid → if-throw, then setter
                if (Boolean.TRUE.equals(item.getIsNonVoid())) {
                    body.addStatement(itemService.getValidationStatement(item));
                }
                generatorSetterToGetter(form, item, body);
            }
        }
        return body;
    }

    private void generatorSetterToGetter(FormDef form, ItemDef item, BlockStmt body) {
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
                getterWithConvert = String.format("LocalDateTime.of(%s, LocalTime.of(0, 0))", getterWithConvert);
            }
            if (itemItem.getFormat() == TimeFormat.TIME) {
                getterWithConvert = String.format("LocalDateTime.of(LocalDate.of(1970, 1, 1), %s)", getterWithConvert);
            }
        }
        body.addStatement(parseStatement(
                "%s.set%s(%s);", form.getVarName(), StringUtils.capitalize(item.getName()),
                getterWithConvert));
    }

}
```

- [ ] **Step 2: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Run form-generator unit tests**

Run: `mvn -pl form-generator -am test`
Expected: BUILD SUCCESS (form-generator has no unit tests of its own, this is a smoke check).

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/SaveApiServiceImpl.java
git commit -m "refactor(form-generator): rewrite SaveApiServiceImpl for 4-case canInputOn* boolean logic"
```

---

## Task 7: Drop the `initPattern`/`editPattern` enum fields and delete `InitOrEditPattern`

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`
- Delete: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/InitOrEditPattern.java`

After all callers are migrated (Tasks 4-6), the enum is unused.

- [ ] **Step 1: Remove the enum fields and related imports from ItemDef.java**

Delete from `ItemDef.java`:
```java
import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.USER_INPUT;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
```

Delete the two existing fields:
```java
    /**
     * 字段的初始化方式
     */
    @NotNull
    InitOrEditPattern initPattern = USER_INPUT;

    /**
     * 字段的编辑方式
     */
    @NotNull
    InitOrEditPattern editPattern = USER_INPUT;
```

- [ ] **Step 2: Delete InitOrEditPattern.java**

Run: `rm form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/InitOrEditPattern.java`

- [ ] **Step 3: Compile**

Run: `mvn -pl form-generator -am compile -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/InitOrEditPattern.java
git commit -m "refactor(form-generator): drop initPattern/editPattern enum fields and delete InitOrEditPattern enum"
```

---

## Task 8: Replace 3 deleted IT cases with 2 new ones — fixtures

**Files:**
- Delete: `allison1875-cli/src/test/resources/it/form-generator/init-pattern-todo/`
- Delete: `allison1875-cli/src/test/resources/it/form-generator/edit-pattern-donot/`
- Delete: `allison1875-cli/src/test/resources/it/form-generator/mixed-init-edit-pattern/`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-edit/forms.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-edit/.allison1875.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-edit/pom.xml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-edit/src/...`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-init/forms.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-init/.allison1875.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-init/pom.xml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/cannot-input-on-init/src/...`

`pom.xml`, `.allison1875.yml`, and the `src/` skeleton are copied from one of the deleted folders (they are identical templates). Use `edit-pattern-donot/` as the source.

- [ ] **Step 1: Copy `edit-pattern-donot` skeleton to two new directories**

```bash
cd allison1875-cli/src/test/resources/it/form-generator
cp -r edit-pattern-donot cannot-input-on-edit
cp -r edit-pattern-donot cannot-input-on-init
```

- [ ] **Step 2: Replace `cannot-input-on-edit/forms.yml`**

Overwrite `cannot-input-on-edit/forms.yml` with:

```yaml
- name: Document
  title: 文档
  desc: 文档管理
  items:
    - type: text
      name: title
      title: 标题
      isNonVoid: true
      maxLength: 200
    - type: text
      name: content
      title: 内容
      isNonVoid: true
      isMultilineOrRich: true
    - type: text
      name: authorName
      title: 作者
      isNonVoid: true
      maxLength: 50
      canInputOnInit: true
      canInputOnEdit: false
  indices: []
```

- [ ] **Step 3: Replace `cannot-input-on-init/forms.yml`**

Overwrite `cannot-input-on-init/forms.yml` with:

```yaml
- name: Task
  title: 任务
  desc: 任务管理
  items:
    - type: text
      name: taskName
      title: 任务名称
      isNonVoid: true
      maxLength: 100
    - type: text
      name: assigneeId
      title: 负责人ID
      isNonVoid: false
      maxLength: 36
      canInputOnInit: false
      canInputOnEdit: true
    - type: text
      name: internalCode
      title: 内部编码
      isNonVoid: true
      maxLength: 36
      canInputOnInit: false
      canInputOnEdit: false
  indices: []
```

- [ ] **Step 4: Delete the three legacy IT directories**

```bash
rm -rf allison1875-cli/src/test/resources/it/form-generator/init-pattern-todo
rm -rf allison1875-cli/src/test/resources/it/form-generator/edit-pattern-donot
rm -rf allison1875-cli/src/test/resources/it/form-generator/mixed-init-edit-pattern
```

- [ ] **Step 5: Commit**

```bash
git add allison1875-cli/src/test/resources/it/form-generator
git commit -m "test(form-generator): replace pattern-enum IT fixtures with cannot-input-on-{edit,init}"
```

---

## Task 9: Replace 3 deleted IT test classes with 2 new ones

**Files:**
- Delete: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/InitPatternTodoItTest.java`
- Delete: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/EditPatternDoNotItTest.java`
- Delete: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/MixedInitEditPatternItTest.java`
- Create: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CannotInputOnEditItTest.java`
- Create: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CannotInputOnInitItTest.java`

- [ ] **Step 1: Delete the three legacy test classes**

```bash
rm allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/InitPatternTodoItTest.java
rm allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/EditPatternDoNotItTest.java
rm allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/MixedInitEditPatternItTest.java
```

- [ ] **Step 2: Create `CannotInputOnEditItTest.java`**

```java
package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * cannot-input-on-edit 集成测试。
 *
 * <p>验证 (canInputOnInit=true, canInputOnEdit=false) 字段：
 * <ul>
 *   <li>该字段出现在 SaveReq DTO 中</li>
 *   <li>由于 isNonVoid 校验改为分支内 if-throw，而非 ReqDTO 注解，所以该字段在 ReqDTO 中无 @NotBlank</li>
 *   <li>setter 仅出现在 if(toCreate) 内</li>
 *   <li>else/edit 分支与 common 节中均不出现该字段</li>
 *   <li>不再生成 // TODO 注释</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnEditItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-edit");

        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveDocumentReq.java");
        assertTrue(saveReqFile.exists(), "SaveDocumentReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());
        assertTrue(saveReqContent.contains("authorName"),
                "SaveReq should contain authorName (canInputOnInit=true)");
        // 校验改为分支内 if-throw，ReqDTO 字段上不应含 @NotBlank
        assertFalse(saveReqContent.contains("@NotBlank String authorName")
                        || saveReqContent.contains("@NotBlank\n    String authorName"),
                "authorName in SaveReq should NOT have @NotBlank when canInputOnEdit=false");

        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveDocumentServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveDocumentServiceImpl should be generated");
        String saveContent = Files.readString(saveFile.toPath());

        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("documentMapper.insert");

        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        String elseSection = saveContent.substring(elseStart, insertCall);
        String commonSection = saveContent.substring(insertCall);

        // toCreate 分支：if-throw 校验 + setter
        assertTrue(toCreateSection.contains("StringUtils.isBlank(req.getAuthorName())"),
                "toCreate should contain isBlank validation for authorName");
        assertTrue(toCreateSection.contains("throw new IllegalArgumentException(\"作者不能为空\")"),
                "toCreate should throw IllegalArgumentException with title");
        assertTrue(toCreateSection.contains("document.setAuthorName("),
                "toCreate should set authorName");

        // else 分支：authorName 不应被设置
        assertFalse(elseSection.contains("document.setAuthorName"),
                "authorName should NOT be set in else branch");

        // common 节（insert 之后到 return 之前）：authorName 不应被设置
        assertFalse(commonSection.contains("document.setAuthorName"),
                "authorName should NOT be set in common section");

        // common section（insert 之前的 common 节，即 else 之后）也不应包含
        assertFalse(elseSection.contains("document.setAuthorName"),
                "authorName should NOT be set in else section");

        // title / content 是 (true,true) 字段，应该在 common 节
        assertTrue(saveContent.contains("document.setTitle(req.getTitle())"),
                "title should be set (in common section)");
        assertTrue(saveContent.contains("document.setContent(req.getContent())"),
                "content should be set (in common section)");

        // 不再生成 TODO 行注释
        assertFalse(saveContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }
}
```

- [ ] **Step 3: Create `CannotInputOnInitItTest.java`**

```java
package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * cannot-input-on-init 集成测试。
 *
 * <p>验证两个组合：
 * <ul>
 *   <li>(canInputOnInit=false, canInputOnEdit=true) — assigneeId（isNonVoid=false）：
 *       出现在 SaveReq；setter 仅在 else 内；isNonVoid=false 故无 if-throw 与默认值赋值</li>
 *   <li>(canInputOnInit=false, canInputOnEdit=false) — internalCode（isNonVoid=true）：
 *       不出现在 SaveReq；toCreate 分支内用 getTodoValue 设置默认值；else/common 中不出现</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnInitItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-init");

        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveTaskReq.java");
        assertTrue(saveReqFile.exists(), "SaveTaskReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());

        assertTrue(saveReqContent.contains("taskName"),
                "SaveReq should contain taskName (canInputOnInit=true)");
        assertTrue(saveReqContent.contains("assigneeId"),
                "SaveReq should contain assigneeId (canInputOnEdit=true)");
        assertFalse(saveReqContent.contains("internalCode"),
                "SaveReq should NOT contain internalCode (both canInputOn* are false)");

        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveTaskServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveTaskServiceImpl should be generated");
        String saveContent = Files.readString(saveFile.toPath());

        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("taskMapper.insert");

        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        String elseSection = saveContent.substring(elseStart, insertCall);

        // (false,false) + isNonVoid=true → toCreate 分支内默认值
        assertTrue(toCreateSection.contains("task.setInternalCode("),
                "toCreate should set internalCode default value");

        // (false,true) + isNonVoid=false → else 内 setter，无 if-throw
        assertTrue(elseSection.contains("task.setAssigneeId(req.getAssigneeId())"),
                "else should set assigneeId from req");
        assertFalse(elseSection.contains("throw new IllegalArgumentException(\"负责人ID不能为空\")"),
                "no validation throw for nullable field");

        // common 节（insert 之后）不出现以上两个 setter
        String afterInsert = saveContent.substring(insertCall);
        assertFalse(afterInsert.contains("task.setAssigneeId"),
                "assigneeId should NOT be set in common section");
        assertFalse(afterInsert.contains("task.setInternalCode"),
                "internalCode should NOT be set in common section");

        // 不再生成 TODO 行注释
        assertFalse(saveContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }
}
```

- [ ] **Step 4: Compile tests**

Run: `mvn -pl allison1875-cli -am test-compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/InitPatternTodoItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/EditPatternDoNotItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/MixedInitEditPatternItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CannotInputOnEditItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CannotInputOnInitItTest.java
git commit -m "test(form-generator): replace pattern-enum IT classes with CannotInputOn{Edit,Init}ItTest"
```

---

## Task 10: Update existing IT test assertions

**Files:**
- Modify: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CommonItemsAutoAddItTest.java`
- Modify: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SaveApiItTest.java`
- Modify: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/README.md`

- [ ] **Step 1: Update `CommonItemsAutoAddItTest.java` Javadoc**

Replace the Javadoc class block (lines 12-29) describing `initPattern=TODO`/`editPattern=DO_NOT` etc. with the new `canInputOn*=false` semantics. Specifically:

Find:
```
 *       在 DDL 中为 {@code VARCHAR(36) NOT NULL}，自动创建唯一索引</li>
 *   <li>{@code createdAt}：自动添加为 items 末尾，initPattern=TODO，editPattern=DO_NOT，
```

Replace with:
```
 *       在 DDL 中为 {@code VARCHAR(36) NOT NULL}，自动创建唯一索引</li>
 *   <li>{@code createdAt}：自动添加为 items 末尾，canInputOnInit=false，canInputOnEdit=false，
```

Find:
```
 *   <li>{@code updatedAt}：自动添加为 items 末尾，initPattern=TODO，editPattern=TODO，
```

Replace with:
```
 *   <li>{@code updatedAt}：自动添加为 items 末尾，canInputOnInit=false，canInputOnEdit=false，
```

Find the inline section comments at lines 82-86:
```java
        // === SaveReq DTO 验证：自动添加字段不应出现 ===
        // === productCode initPattern=TODO → 不进入 SaveReq
        // === createdAt   initPattern=TODO → 不进入 SaveReq
        // === updatedAt   initPattern=TODO → 不进入 SaveReq
```
Replace with:
```java
        // === SaveReq DTO 验证：自动添加字段不应出现 ===
        // === productCode canInputOn*=false → 不进入 SaveReq（但被显式追加为 bizId 字段）
        // === createdAt   canInputOn*=false → 不进入 SaveReq
        // === updatedAt   canInputOn*=false → 不进入 SaveReq
```

Find line 105-108:
```java
        // createdAt/updatedAt 不应出现在 SaveReq 中
        assertFalse(saveReqContent.contains("createdAt"),
                "SaveReq should NOT contain createdAt (initPattern=TODO)");
        assertFalse(saveReqContent.contains("updatedAt"),
                "SaveReq should NOT contain updatedAt (initPattern=TODO)");
```
Replace with:
```java
        // createdAt/updatedAt 不应出现在 SaveReq 中
        assertFalse(saveReqContent.contains("createdAt"),
                "SaveReq should NOT contain createdAt (canInputOn*=false)");
        assertFalse(saveReqContent.contains("updatedAt"),
                "SaveReq should NOT contain updatedAt (canInputOn*=false)");
```

Find line 138-142:
```java
        // 确保 product.setCreatedAt 只在 toCreate 分支出现（editPattern=DO_NOT）
        int elseIndex = saveContent.indexOf("} else {");
        String elseContent = saveContent.substring(elseIndex);
        assertFalse(elseContent.contains("product.setCreatedAt("),
                "createdAt should NOT be set in else/edit branch (editPattern=DO_NOT)");
```
Replace with:
```java
        // 确保 product.setCreatedAt 只在 toCreate 分支出现（canInputOnEdit=false）
        int elseIndex = saveContent.indexOf("} else {");
        String elseContent = saveContent.substring(elseIndex);
        assertFalse(elseContent.contains("product.setCreatedAt("),
                "createdAt should NOT be set in else/edit branch (canInputOnEdit=false)");
```

- [ ] **Step 2: Update `SaveApiItTest.java`**

The SaveApiItTest's `forms.yml` (in `allison1875-cli/src/test/resources/it/form-generator/save-api/forms.yml`) does not currently use `initPattern`/`editPattern` (all fields default to userInput → all (true,true) post-migration). Verify by reading the file:

Run: `cat allison1875-cli/src/test/resources/it/form-generator/save-api/forms.yml | grep -E "initPattern|editPattern"`
Expected: no output.

If the grep finds matches, replace `initPattern: userInput` → `canInputOnInit: true` and `editPattern: userInput` → `canInputOnEdit: true` in that file. (These are pure rename edits.)

The Java test class itself does not reference `initPattern`/`editPattern` literally, so no Java edits needed.

- [ ] **Step 3: Update README.md**

In `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/README.md`, find any rows mentioning `init-pattern-todo`, `edit-pattern-donot`, `mixed-init-edit-pattern` and replace with `cannot-input-on-edit`, `cannot-input-on-init` (only 2 entries instead of 3). If the README lists test cases by title or a table, update accordingly.

Run: `grep -n "init-pattern-todo\|edit-pattern-donot\|mixed-init-edit-pattern\|InitPatternTodo\|EditPatternDoNot\|MixedInitEditPattern" allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/README.md`

For each match, replace with the equivalent `cannot-input-on-{edit,init}` description; merge or delete redundant lines.

- [ ] **Step 4: Compile**

Run: `mvn -pl allison1875-cli -am test-compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/CommonItemsAutoAddItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SaveApiItTest.java \
        allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/README.md \
        allison1875-cli/src/test/resources/it/form-generator/save-api/forms.yml
git commit -m "test(form-generator): align CommonItemsAutoAdd/SaveApi/README to canInputOn* terminology"
```

---

## Task 11: Run all form-generator IT tests

**Files:** none modified.

- [ ] **Step 1: Run only the form-generator IT tests**

Run: `mvn -pl allison1875-cli -am test -Dtest='com.spldeolin.allison1875.cli.it.formgenerator.*ItTest'`
Expected: BUILD SUCCESS, all formgenerator IT tests pass (including the two new ones).

- [ ] **Step 2: If any test fails, fix and recommit**

If a test fails because the generated code differs from the assertion, **debug**:
1. Inspect the generated file under the test's `basedir` (printed in stack trace).
2. Either fix the assertion (if the new behavior is correct per spec) or fix `SaveApiServiceImpl` (if a generation bug slipped through).
3. Recommit with `fix(form-generator): <what you fixed>`.

- [ ] **Step 3: No commit if tests pass.**

---

## Task 12: Spec & skill rename-only edits

**Files:** (rename `initPattern`/`editPattern`/`InitOrEditPattern`/value-token-strings)

- Modify: `docs/superpowers/specs/2026-05-23-form-web-design.md`
- Modify: `docs/superpowers/specs/2026-05-24-app-generator-design.md`
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator-design.md`
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md`
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md`
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md`
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`
- Modify: `docs/superpowers/plans/2026-05-23-form-web-implementation.md`
- Modify: `docs/superpowers/plans/2026-05-24-app-generator-implementation.md`
- Modify: `docs/superpowers/plans/2026-05-28-aligning-frontend-with-form-generator-implementation.md`
- Modify: `skills/generate-form/SKILL.md`
- Modify: `skills/integrate-allison1875/examples/forms.yml`

The substitutions are mechanical:

| From | To |
|:--|:--|
| `initPattern` | `canInputOnInit` |
| `editPattern` | `canInputOnEdit` |
| `InitOrEditPattern` | `Boolean` |
| (value) `userInput` | `true` |
| (value) `doNot` | `false` |
| (value) `todo` | `false` |

Note: the value-token replacements (`userInput`/`doNot`/`todo`) only matter inside YAML/code blocks where they appear as values for the renamed fields. Don't blindly replace these strings in prose where they describe historical behavior.

- [ ] **Step 1: Use sed for the field-name renames in all spec/skill files**

```bash
cd /Users/Deolin/Documents/project-repo/github/allison1875

# Field-name renames (safe globally — these tokens only appear as field names)
for f in \
  docs/superpowers/specs/2026-05-23-form-web-design.md \
  docs/superpowers/specs/2026-05-24-app-generator-design.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator-design.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml \
  docs/superpowers/plans/2026-05-23-form-web-implementation.md \
  docs/superpowers/plans/2026-05-24-app-generator-implementation.md \
  docs/superpowers/plans/2026-05-28-aligning-frontend-with-form-generator-implementation.md \
  skills/generate-form/SKILL.md \
  skills/integrate-allison1875/examples/forms.yml \
; do
  sed -i '' 's/initPattern/canInputOnInit/g; s/editPattern/canInputOnEdit/g; s/InitOrEditPattern/Boolean/g' "$f"
done
```

- [ ] **Step 2: Manually update value tokens in YAML blocks**

For each file, scan for `canInputOnInit:` and `canInputOnEdit:` lines and rewrite their values:

```
canInputOnInit: userInput   →   canInputOnInit: true
canInputOnInit: doNot       →   canInputOnInit: false
canInputOnInit: todo        →   canInputOnInit: false
canInputOnEdit: userInput   →   canInputOnEdit: true
canInputOnEdit: doNot       →   canInputOnEdit: false
canInputOnEdit: todo        →   canInputOnEdit: false
```

Run as a second sed pass restricted to those lines:

```bash
for f in \
  docs/superpowers/specs/2026-05-23-form-web-design.md \
  docs/superpowers/specs/2026-05-24-app-generator-design.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator-design.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md \
  docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml \
  docs/superpowers/plans/2026-05-23-form-web-implementation.md \
  docs/superpowers/plans/2026-05-24-app-generator-implementation.md \
  docs/superpowers/plans/2026-05-28-aligning-frontend-with-form-generator-implementation.md \
  skills/generate-form/SKILL.md \
  skills/integrate-allison1875/examples/forms.yml \
; do
  sed -i '' \
    -e '/canInputOnInit:/s/userInput/true/g' \
    -e '/canInputOnInit:/s/doNot/false/g' \
    -e '/canInputOnInit:/s/todo/false/g' \
    -e '/canInputOnEdit:/s/userInput/true/g' \
    -e '/canInputOnEdit:/s/doNot/false/g' \
    -e '/canInputOnEdit:/s/todo/false/g' \
    "$f"
done
```

- [ ] **Step 3: Manually inspect the result**

Run: `grep -n 'canInputOn' docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`
Expected: All values are `true` or `false`, no leftover `userInput`/`doNot`/`todo`.

For prose mentions (e.g. "initPattern=todo" inside Markdown text or comments) sed already converted "initPattern" → "canInputOnInit". Now scan for any remaining `=todo` / `=doNot` / `=userInput` constructs in those files and update them:

```bash
grep -rn 'canInputOn[A-Z][a-z]*=\(todo\|doNot\|userInput\)' docs/superpowers/specs docs/superpowers/plans skills/
```

For each match, manually edit the line to use `=true` / `=false` consistent with the field's intended boolean.

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs docs/superpowers/plans skills/generate-form/SKILL.md skills/integrate-allison1875/examples/forms.yml
git commit -m "docs: rename initPattern/editPattern to canInputOnInit/canInputOnEdit across specs, plans, skills"
```

---

## Task 13: Update `super-dsl.yml` to cover all 4 boolean combinations

**Files:**
- Modify: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`

After Task 12, the file has been mechanically renamed. Now add explicit coverage for case `(false,true)`.

- [ ] **Step 1: Inspect the migrated super-dsl.yml**

Run: `cat docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml | grep -nE 'canInputOn|name:'`
Verify: gender field has `(true,false)`; emergencyContact has `(false,false)`; no field currently has `(false,true)`.

- [ ] **Step 2: Add a `(false,true)` field to StudentDormitory**

Open `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`.

Locate the `# secret, canInputOnInit=false, canInputOnEdit=false` block (around line 132 — that's the `emergencyContact` block, which now reads `false/false`). After it, before the `monthlyRent` block, **insert** a new field:

```yaml

        # text, canInputOnInit=false, canInputOnEdit=true (assignment after creation)
        - type: text
          name: assignedRoom
          title: 分配房间号
          isNonVoid: true
          maxLength: 32
          canInputOnInit: false
          canInputOnEdit: true
```

- [ ] **Step 3: Update the form-level coverage comment at the top of the file**

In the leading block comment (lines 1-15), update Form 2's description to mention the new field. Replace:
```
# Form 2: StudentDormitory
#   Coverage: multiSelect + onOff + secret (userInput+userInput, todo+doNot) + number (decimal)
#             composite unique index
```

with:

```
# Form 2: StudentDormitory
#   Coverage: multiSelect + onOff + secret (true+true, false+false) + text (false+true) + number (decimal)
#             composite unique index
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml
git commit -m "docs(super-dsl): add (canInputOnInit=false, canInputOnEdit=true) coverage via assignedRoom field"
```

---

## Task 14: Final full-build sanity check

**Files:** none modified.

- [ ] **Step 1: Run the full test suite**

Run: `mvn -am -pl form-generator,allison1875-cli test`
Expected: BUILD SUCCESS.

- [ ] **Step 2: Re-grep for stale references**

Run:
```bash
grep -rn 'initPattern\|editPattern\|InitOrEditPattern' \
  --include='*.java' --include='*.md' --include='*.yml' \
  /Users/Deolin/Documents/project-repo/github/allison1875 \
  | grep -v '/target/' | grep -v '/output/'
```

Expected: no output (or only output that is intentional historical text in plan documents — which we already did include).

If there are remaining occurrences in main source / IT / spec / skill, decide case-by-case whether they need fixing and amend the appropriate task's commit.

- [ ] **Step 3: Final commit (only if there were grep-driven fixes)**

If anything had to be fixed, commit it as `chore: clean up stale initPattern/editPattern references`.

---

## Self-review notes

- **Spec coverage check:** §3 → Task 1, 4, 5, 7. §4 → Task 2, 3, 6. §5 → Task 8, 9, 10, 11. §6 → Task 12. §7 → Task 13. ✓
- **Type consistency:** `getValidationStatement(I)` returns `com.github.javaparser.ast.stmt.Statement` — referenced consistently across Tasks 2, 3, 6.
- **Boolean default:** ItemDef boolean defaults to `true` (matches "保留旧默认 USER_INPUT 行为").
- **Risk of `Boolean.TRUE.equals(...)` vs `getXxx()` direct call:** I used `Boolean.TRUE.equals(...)` to defensively handle null (in case Jackson defaults bypass `=true` initialization for explicitly absent YAML keys). Validates as expected once `@NotNull` validation passes.
