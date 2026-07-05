# Secret 字段编辑回显与可选覆盖 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 `secret` 字段在编辑弹框中可回显占位并支持"不修改即保留原值"，同时清理遗留死代码 `getValidationStatement`。

**Architecture:** 后端 form-generator 的 `UpdateApiServiceImpl` 对 `secret` 字段改为「EditReqDTO 不加 `@NotEmpty` + Update 方法体生成 null-skip 包裹块」；前端 frontend-skeleton 的 4 个 core 文件调整可见性/必填规则并让 `SecretField` 区分 create/edit-update 渲染。null(不改)/""(清空)/串(覆盖) 三态协议贯穿前后端。

**Tech Stack:** Java 21 · JavaParser 3.28.1 · Google Guice · JUnit 5 IT · Vue 3 + naive-ui + TypeScript

**Spec:** `docs/superpowers/specs/2026-07-05-secret-field-edit-echo-design.md`

---

## File Structure

**后端（form-generator）**
- `service/impl/UpdateApiServiceImpl.java` — 改 2 处：EditReqDTO 跳过 secret 的校验注解；Update 方法体为 secret 生成 null-skip 块。
- `service/ItemService.java` — 删 `getValidationStatement` 接口声明 + 相关 import。
- `service/impl/{Primary,Select,MultiSelect,Text,Number,Time,OnOff,Secret,File}ItemService*.java` — 删 `getValidationStatement` 实现 + 清理孤立 import。

**前端（app-generator/frontend-skeleton/src/core）**
- `protocol/field-policy.ts` — `isVisible` 的 edit-update 分支隐藏 `canInputOnEdit=false` 的 secret。
- `EditModal.vue` — required 规则/红星排除 edit-update 的 secret；向 FieldRenderer 透传 `editMode`。
- `fields/FieldRenderer.vue` — 新增 `editMode` prop，仅 secret 透传。
- `fields/SecretField.vue` — 区分 create / edit-update 渲染，含占位符与条件清空按钮。

**测试**
- `allison1875-cli/src/test/resources/it/form-generator/secret-edit-echo/` — 新 IT case 资源。
- `allison1875-cli/src/test/java/.../cli/it/formgenerator/SecretEditEchoItTest.java` — 新 IT。

**文档**
- `form-generator/CLAUDE.md` — 更新 `secret` 类型描述。

---

## Task 1: 后端 — EditReqDTO 不为 secret 生成 @NotEmpty

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java:60-71`

- [ ] **Step 1: 修改 generateUpdateInitDec，secret 跳过校验注解**

在 `UpdateApiServiceImpl.generateUpdateInitDec` 中，把当前循环体（60-71 行）：

```java
        for (ItemDef item : form.getItems()) {
            // 字段进入 ReqDTO 当且仅当编辑时允许用户输入
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                FieldDeclaration itemField = parseFieldDeclaration(
                        itemService.getJavaTypeInDTO(item) + " " + item.getName() + ";");
                JavadocUtils.setJavadoc(itemField, item.getTitle(), null);
                // 所有 canInputOnEdit 字段直接添加校验注解到 ReqDTO 字段
                itemService.getJavaValidAnnotations(item).forEach(itemField::addAnnotation);
                itemService.getJavaJsonFormatAnnoatation(item).ifPresent(itemField::addAnnotation);
                reqCoid.addMember(itemField);
            }
        }
```

改为（仅新增对 secret 的校验注解跳过；其余不变）：

```java
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
```

> `ItemType` 已在文件顶部 import（第 21 行），无需新增。

- [ ] **Step 2: 编译验证**

Run: `mvn -q -pl form-generator -am compile`
Expected: BUILD SUCCESS（本步与 Task 2 的方法体改动独立，可单独编译通过）

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java
git commit -m "feat: omit @NotEmpty on secret field in EditReqDTO"
```

---

## Task 2: 后端 — Update 方法体为 secret 生成 null-skip 块

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java:92-100`

- [ ] **Step 1: 修改 generateUpdateMethodBody 的 setter 循环**

当前 92-100 行：

```java
        // 3. Set fields where canInputOnEdit=true (non-audited, non-multiSelect)
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                mutationApiSupport.generateSetterToGetter(form, item, body);
            }
        }
```

改为（secret 分支生成 null-skip 块，其余字段行为不变）：

```java
        // 3. Set fields where canInputOnEdit=true (non-audited, non-multiSelect)
        for (ItemDef item : form.getNonAuditedItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getCanInputOnEdit())) {
                if (item.getType() == ItemType.SECRET) {
                    generateSecretNullSkipSetter(form, item, body);
                } else {
                    mutationApiSupport.generateSetterToGetter(form, item, body);
                }
            }
        }
```

- [ ] **Step 2: 新增私有方法 generateSecretNullSkipSetter**

在 `UpdateApiServiceImpl` 类内、`generateUpdateMethodBody` 方法之后，新增私有方法：

```java
    /**
     * secret 字段编辑语义：null=不修改(跳过 setter)；""=清空；非空串=覆盖。
     * isNonVoid=true 时在覆盖分支前拒绝空串。
     */
    private void generateSecretNullSkipSetter(FormDef form, ItemDef item, BlockStmt body) {
        String getter = String.format("req.get%s()", StringUtils.capitalize(item.getName()));
        String setter = String.format("%s.set%s(%s)", form.getVarName(),
                StringUtils.capitalize(item.getName()), getter);
        if (Boolean.TRUE.equals(item.getIsNonVoid())) {
            body.addStatement(parseStatement(String.format(
                    "if (%s != null) { if (%s.isEmpty()) { throw new %s(\"%s不能为空\"); } %s; }",
                    getter, getter, config.getCodeSnippet().getBizExceptionQualifier(),
                    item.getTitle(), setter)));
        } else {
            body.addStatement(parseStatement(String.format(
                    "if (%s != null) { %s; }", getter, setter)));
        }
    }
```

> `StringUtils`（org.apache.commons.lang3，第 6 行）、`parseStatement`（静态导入，第 4 行）、`BlockStmt`、`FormDef`、`ItemDef`、`config` 字段均已在文件中可用，无需新增 import。

- [ ] **Step 3: 编译验证**

Run: `mvn -q -pl form-generator -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java
git commit -m "feat: generate null-skip setter for secret field on update"
```

---

## Task 3: 后端 — 新增 IT 用例资源

**Files:**
- Create: `allison1875-cli/src/test/resources/it/form-generator/secret-edit-echo/.allison1875.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/secret-edit-echo/forms.yml`
- Create: `allison1875-cli/src/test/resources/it/form-generator/secret-edit-echo/pom.xml`

- [ ] **Step 1: 创建 .allison1875.yml**

内容（复制自 cannot-input-on-init，仅改 domain name）：

```yaml
# form-generator secret-edit-echo integration test config
domains:
  - name: secret-edit-echo
    controllerModule: "."
    controllerPackage: com.example.controller
    dtoModule: "."
    reqDTOPackage: com.example.dto.req
    respDTOPackage: com.example.dto.resp
    enumModule: "."
    enumPackage: com.example.enums
    serviceModule: "."
    servicePackage: com.example.service
    serviceImplModule: "."
    serviceImplPackage: com.example.service.impl
    persistenceModule: "."
    mapperPackage: com.example.mapper
    entityPackage: com.example.entity
    designPackage: com.example.design
    paramDTOPackage: com.example.dto.param
    recordDTOPackage: com.example.dto.record
    wholeDTOPackage: com.example.dto
    mapperXmlDirs:
      - src/main/resources/mapper
author: test-author
enableNoModifyAnnounce: false
enableJavaxMoveToJakarta: false
isDataModelWithoutLombok: false
enableGenerateDesign: true
isEntityEndWithEntity: true
dslPath: forms.yml
codeSnippet:
  controllerRequestMapping: "/api/v1/${formName}"
  shortUuidGeneration: "UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()"
  collectionEmptyCheck: "${list} == null || ${list}.isEmpty()"
```

- [ ] **Step 2: 创建 forms.yml**

含两个 secret 字段：`apiKey`（isNonVoid=true）与 `backupToken`（isNonVoid=false），均 canInputOnEdit=true（默认）；外加一个普通 text 字段作对照。

```yaml
- name: Credential
  title: 凭据
  desc: 凭据管理
  items:
    - type: text
      name: credName
      title: 凭据名称
      isNonVoid: true
      maxLength: 100
    - type: secret
      name: apiKey
      title: API密钥
      isNonVoid: true
    - type: secret
      name: backupToken
      title: 备用令牌
      isNonVoid: false
  indices: []
```

- [ ] **Step 3: 创建 pom.xml**

内容（复制自 cannot-input-on-init，仅改 artifactId）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.spldeolin.allison1875.it</groupId>
    <artifactId>form-generator-secret-edit-echo</artifactId><version>1.0-SNAPSHOT</version>
    <properties><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><maven.compiler.source>21</maven.compiler.source><maven.compiler.target>21</maven.compiler.target></properties>
    <dependencies>
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>1.18.30</version><scope>provided</scope></dependency>
        <dependency>
            <groupId>com.spldeolin.allison1875</groupId>
            <artifactId>allison1875-support</artifactId>
            <version>14.0-SNAPSHOT</version>
        </dependency>
        <dependency><groupId>org.springframework</groupId><artifactId>spring-webmvc</artifactId><version>5.3.31</version></dependency>
        <dependency><groupId>javax.validation</groupId><artifactId>validation-api</artifactId><version>2.0.1.Final</version></dependency>
        <dependency><groupId>org.mybatis</groupId><artifactId>mybatis</artifactId><version>3.5.14</version></dependency>
        <dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-annotations</artifactId><version>2.13.5</version></dependency>
        <dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId><version>2.13.5</version></dependency>
        <dependency><groupId>javax.annotation</groupId><artifactId>javax.annotation-api</artifactId><version>1.3.2</version></dependency>
        <dependency><groupId>org.springframework</groupId><artifactId>spring-tx</artifactId><version>5.3.31</version></dependency>
        <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-api</artifactId><version>2.0.9</version></dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: Commit**

```bash
git add allison1875-cli/src/test/resources/it/form-generator/secret-edit-echo/
git commit -m "test: add secret-edit-echo IT fixtures"
```

---

## Task 4: 后端 — 新增 IT 断言

**Files:**
- Create: `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SecretEditEchoItTest.java`

- [ ] **Step 1: 编写 IT（先写断言，确认能跑起来并失败/通过）**

```java
package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * secret-edit-echo 集成测试。
 *
 * <p>验证 canInputOnEdit=true 的 secret 字段编辑语义：
 * <ul>
 *   <li>EditReqDTO 中 secret 字段不带 @NotEmpty（null 表示不修改）</li>
 *   <li>CreateReqDTO 中 secret 字段仍带 @NotEmpty（创建必填）</li>
 *   <li>Update 方法体对 secret 生成 null-skip 块；isNonVoid=true 版含空串校验</li>
 * </ul>
 *
 * @author Deolin 2026-07-05
 */
public class SecretEditEchoItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("secret-edit-echo");

        // CreateReq: secret 字段仍带 @NotEmpty
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateCredentialReq.java");
        assertTrue(createReqFile.exists(), "CreateCredentialReq DTO should be generated");
        String createReq = Files.readString(createReqFile.toPath());
        assertTrue(createReq.contains("apiKey"), "CreateReq should contain apiKey");
        assertTrue(createReq.contains("@NotEmpty"), "CreateReq secret should keep @NotEmpty");

        // UpdateReq: secret 字段无 @NotEmpty
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateCredentialReq.java");
        assertTrue(updateReqFile.exists(), "UpdateCredentialReq DTO should be generated");
        String updateReq = Files.readString(updateReqFile.toPath());
        assertTrue(updateReq.contains("apiKey"), "UpdateReq should contain apiKey");
        assertTrue(updateReq.contains("backupToken"), "UpdateReq should contain backupToken");
        assertFalse(updateReq.contains("@NotEmpty"),
                "UpdateReq secret fields should NOT carry @NotEmpty");

        // UpdateServiceImpl: null-skip 块
        File updateFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateCredentialServiceImpl.java");
        assertTrue(updateFile.exists(), "UpdateCredentialServiceImpl should be generated");
        String update = Files.readString(updateFile.toPath());
        // 无条件 setter 不应出现
        assertFalse(update.contains("credential.setApiKey(req.getApiKey());\n"),
                "secret should not be set unconditionally");
        // isNonVoid=true 的 apiKey：null-skip + 空串校验 + setter
        assertTrue(update.contains("if (req.getApiKey() != null)"),
                "apiKey update should be null-guarded");
        assertTrue(update.contains("req.getApiKey().isEmpty()"),
                "apiKey update should reject empty string (isNonVoid=true)");
        assertTrue(update.contains("credential.setApiKey(req.getApiKey())"),
                "apiKey should be set inside guard");
        // isNonVoid=false 的 backupToken：null-skip + setter，但无空串校验
        assertTrue(update.contains("if (req.getBackupToken() != null)"),
                "backupToken update should be null-guarded");
        assertFalse(update.contains("req.getBackupToken().isEmpty()"),
                "backupToken (isNonVoid=false) should NOT reject empty string");
        assertTrue(update.contains("credential.setBackupToken(req.getBackupToken())"),
                "backupToken should be set inside guard");

        // GetDetail 仍排除 secret（安全要求，回显根因）
        File detailRespFile = new File(basedir,
                "src/main/java/com/example/dto/resp/GetCredentialDetailResp.java");
        assertTrue(detailRespFile.exists(), "GetCredentialDetailResp should be generated");
        String detailResp = Files.readString(detailRespFile.toPath());
        assertFalse(detailResp.contains("apiKey"), "detail resp should exclude secret apiKey");
        assertFalse(detailResp.contains("backupToken"), "detail resp should exclude secret backupToken");
    }
}
```

> DTO/Service 文件名规则来自 form-generator：req 局部类 `req` → `Create{Form}Req` / `Update{Form}Req`；
> detail resp → `Get{Form}DetailResp`；service impl → `Update{Form}ServiceImpl`。已按现有 IT
> （CannotInputOnInitItTest 用 `UpdateTaskReq`/`UpdateTaskServiceImpl`）核实命名模式。

- [ ] **Step 2: 运行新 IT**

Run: `mvn test -pl allison1875-cli -am -Dtest=SecretEditEchoItTest`
Expected: PASS（Task 1/2 已实现后端逻辑）

> 若 detail resp 文件名断言失败（找不到文件），先用
> `find target/it/secret-edit-echo -name "*Detail*"` 核实实际生成名再修正断言路径。

- [ ] **Step 3: Commit**

```bash
git add allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/SecretEditEchoItTest.java
git commit -m "test: assert secret field edit-echo backend generation"
```

---

## Task 5: 后端 — 清理死代码 getValidationStatement（接口）

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/ItemService.java`

- [ ] **Step 1: 删除接口方法声明与 Statement import**

删除 `ItemService.java` 中的方法声明（60-68 行整段 Javadoc + 声明）：

```java
    /**
     * 生成"如果字段为空则抛出异常"的校验语句，用于条件分支内的延迟校验。
     * 示例：if (req.getXxx() == null) { throw new IllegalArgumentException("xxx不能为空"); }
     *
     * 仅在 isNonVoid==true 且字段处于 (canInputOnInit=true,canInputOnEdit=false) 或
     * (canInputOnInit=false,canInputOnEdit=true) 组合时调用。
     */
    Statement getValidationStatement(I itemDef);
```

同时删除第 6 行的 import（`Statement` 在接口中已无其他使用）：

```java
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 2: 暂不编译**（实现类仍带 @Override，需 Task 6 一并删除后再编译）

进入 Task 6。

---

## Task 6: 后端 — 清理死代码 getValidationStatement（所有实现）

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java`
- Modify: `.../service/impl/SecretItemService.java`
- Modify: `.../service/impl/SelectItemService.java`
- Modify: `.../service/impl/MultiSelectItemService.java`
- Modify: `.../service/impl/TextItemService.java`
- Modify: `.../service/impl/NumberItemService.java`
- Modify: `.../service/impl/TimeItemService.java`
- Modify: `.../service/impl/OnOffItemService.java`
- Modify: `.../service/impl/FileItemService.java`

- [ ] **Step 1: PrimaryItemServiceImpl — 删方法 + Statement import**

删除方法（含 @Override）：

```java
    @Override
    public Statement getValidationStatement(ItemDef itemDef) {
        return delegate(itemDef).getValidationStatement(itemDef);
    }
```

删除 import `import com.github.javaparser.ast.stmt.Statement;`（该文件 `Statement` 仅此一处使用）。

- [ ] **Step 2: SecretItemService — 删方法 + 清理 import**

删除方法（含 @Override，含 `if (!StringUtils.hasText...` 那段），以及方法前的 Javadoc（若有）。删除后清理以下 import（该文件中 `parseStatement`、`Statement`、`StringUtils`、`org.apache.commons.lang3.StringUtils` 仅在该方法中使用）：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

> 保留 `MoreStringUtils`（第 46 行 getDbColumnName 使用）。

- [ ] **Step 3: SelectItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 bare `StringUtils.` 仅第 89 行（被删方法）使用（其余为 `MoreStringUtils`），故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 4: MultiSelectItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。清理 import：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import com.github.javaparser.ast.stmt.Statement;
```

> **保留** `import org.apache.commons.lang3.StringUtils;` — 第 145 行 `StringUtils.capitalize(item.getName())` 仍在使用（关联表单命名）。

- [ ] **Step 5: TextItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 `StringUtils.` 仅在被删方法（第 84-85 行）使用，故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 6: NumberItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 `StringUtils.` 仅在被删方法（第 87 行）使用，故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 7: TimeItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 `StringUtils.` 仅在被删方法（第 88 行）使用，故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

> **保留** `parseAnnotation` 静态导入（TimeItemService 另有使用）。仅删 `parseStatement`。

- [ ] **Step 8: OnOffItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 `StringUtils.` 仅在被删方法（第 83 行）使用，故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 9: FileItemService — 删方法 + 清理 import**

删除 `getValidationStatement` 方法（含 @Override）。该文件 `StringUtils.` 仅在被删方法（第 81-82 行）使用，故清理：

```java
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.stmt.Statement;
```

- [ ] **Step 10: 全量编译验证**

Run: `mvn -q -pl form-generator -am compile`
Expected: BUILD SUCCESS（无 "unused import" 错误，无 @Override 悬空）

> 若某文件报 `StringUtils` 仍被使用，说明该文件另有 bare `StringUtils.` 调用——恢复其 import 即可。

- [ ] **Step 11: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/ItemService.java \
        form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/
git commit -m "refactor: remove dead getValidationStatement from ItemService"
```

---

## Task 7: 前端 — field-policy 隐藏不可编辑 secret

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts:52-57`

- [ ] **Step 1: 在 edit-update 分支加 secret 专属隐藏规则**

当前 `isVisible` 的 `edit-update` case（52-57 行）：

```typescript
    case 'edit-update':
      // canInputOnEdit=false: field is read-only in edit mode — still show as display-only
      // (frontend shows it but doesn't submit it; backend ignores the field if sent)
      // canInputOnInit=false AND canInputOnEdit=false: completely backend-managed, hide
      if (!canInputOnInit && !canInputOnEdit) return false
      return true
```

改为（新增 secret 且不可编辑则隐藏；其余类型行为不变）：

```typescript
    case 'edit-update':
      // secret 字段编辑不可修改时直接隐藏（不做只读展示，且详情接口本就不返回明文）
      if (item.type === 'secret' && !canInputOnEdit) return false
      // canInputOnEdit=false: field is read-only in edit mode — still show as display-only
      // (frontend shows it but doesn't submit it; backend ignores the field if sent)
      // canInputOnInit=false AND canInputOnEdit=false: completely backend-managed, hide
      if (!canInputOnInit && !canInputOnEdit) return false
      return true
```

- [ ] **Step 2: TypeScript 类型检查（若前端骨架有 tsconfig/vue-tsc）**

Run: `cd app-generator/src/main/resources/frontend-skeleton && ls package.json 2>/dev/null && echo "has package.json" || echo "no build here — rely on review"`
Expected: 打印结果；骨架为静态资源模板，通常无本地 node_modules → 靠类型标注 + 人工审查。不强制运行构建。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts
git commit -m "feat: hide non-editable secret field in edit modal"
```

---

## Task 8: 前端 — EditModal 排除 secret 的必填校验并透传 editMode

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/EditModal.vue:45-71,106-112`

- [ ] **Step 1: rules computed 排除 edit-update 的 secret**

当前 `rules` computed（45-71 行）的循环开头判断：

```typescript
  for (const item of visibleItems.value) {
    // Only add validation rules for editable fields
    if (item.isNonVoid && isEditable(item, editMode.value)) {
```

改为在该 `if` 前插入 secret 的 edit-update 跳过：

```typescript
  for (const item of visibleItems.value) {
    // 编辑已有记录时，secret 允许"不修改"（提交 null），因此不生成必填规则
    if (item.type === 'secret' && editMode.value === 'edit-update') continue
    // Only add validation rules for editable fields
    if (item.isNonVoid && isEditable(item, editMode.value)) {
```

- [ ] **Step 2: NFormItem 的 :required 同步排除**

当前模板（104 行）：

```vue
            :required="item.isNonVoid && isEditable(item, editMode)"
```

改为：

```vue
            :required="item.isNonVoid && isEditable(item, editMode) && !(item.type === 'secret' && editMode === 'edit-update')"
```

- [ ] **Step 3: 向 FieldRenderer 透传 editMode**

当前 FieldRenderer 调用（106-112 行）：

```vue
            <FieldRenderer
              :item="item"
              :mode="isEditable(item, editMode) ? 'edit' : 'display'"
              :value="localModel[item.name] ?? null"
              :readonly="!isEditable(item, editMode)"
              @update:value="isEditable(item, editMode) ? updateField(item.name, $event) : undefined"
            />
```

改为（新增 `:edit-mode`）：

```vue
            <FieldRenderer
              :item="item"
              :mode="isEditable(item, editMode) ? 'edit' : 'display'"
              :edit-mode="editMode"
              :value="localModel[item.name] ?? null"
              :readonly="!isEditable(item, editMode)"
              @update:value="isEditable(item, editMode) ? updateField(item.name, $event) : undefined"
            />
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/EditModal.vue
git commit -m "feat: exclude edit-update secret from required rules and pass editMode"
```

---

## Task 9: 前端 — FieldRenderer 透传 editMode 给 SecretField

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue`

- [ ] **Step 1: props 新增 editMode**

当前 defineProps：

```typescript
const props = defineProps<{
  item: ItemDef
  mode: 'search' | 'edit' | 'display'
  value: any
  /** In an edit modal, a visible-but-not-editable field is read-only (file field renders a distinct style). */
  readonly?: boolean
}>()
```

改为新增可选 `editMode`：

```typescript
const props = defineProps<{
  item: ItemDef
  mode: 'search' | 'edit' | 'display'
  value: any
  /** In an edit modal, a visible-but-not-editable field is read-only (file field renders a distinct style). */
  readonly?: boolean
  /** 弹框场景（create/update），仅 secret 字段消费以区分渲染。 */
  editMode?: 'edit-create' | 'edit-update'
}>()
```

- [ ] **Step 2: extraProps 为 secret 追加 editMode**

当前：

```typescript
const extraProps = computed(() => (props.item.type === 'file' ? { readonly: props.readonly } : {}))
```

改为：

```typescript
const extraProps = computed(() => {
  if (props.item.type === 'file') return { readonly: props.readonly }
  if (props.item.type === 'secret') return { editMode: props.editMode }
  return {}
})
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue
git commit -m "feat: forward editMode to SecretField in FieldRenderer"
```

---

## Task 10: 前端 — SecretField 区分 create / edit-update 渲染

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/fields/SecretField.vue`

- [ ] **Step 1: 重写 SecretField.vue**

完整替换文件内容：

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { NInput, NButton, NSpace } from 'naive-ui'
import type { SecretItemDef } from '@/schema/types'

const props = defineProps<{
  item: SecretItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
  /** 弹框场景：'edit-create'（创建）或 'edit-update'（编辑已有记录）。 */
  editMode?: 'edit-create' | 'edit-update'
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()

// 编辑已有记录时的特殊态：详情接口不返回明文，允许"不修改即保留"。
const isEditUpdate = computed(() => props.mode === 'edit' && props.editMode === 'edit-update')

// 仅非必填 secret 显示清空按钮（必填 secret 清空必被后端拒绝）。
const showClearButton = computed(() => isEditUpdate.value && props.item.isNonVoid === false)

function onClear() {
  emit('update:value', '')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>***</span>
  </template>
  <template v-else-if="mode === 'edit' && isEditUpdate">
    <NSpace align="center" :wrap="false" style="width: 100%">
      <NInput
        type="password"
        :value="value"
        show-password-on="click"
        placeholder="••••••"
        style="flex: 1"
        @update:value="emit('update:value', $event)"
      />
      <NButton v-if="showClearButton" quaternary size="small" @click="onClear">
        清空
      </NButton>
    </NSpace>
  </template>
  <template v-else-if="mode === 'edit'">
    <NInput
      type="password"
      :value="value"
      show-password-on="click"
      clearable
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

> 说明：
> - `display` 模式不变（列表/详情脱敏为 `***`）。
> - `edit` + `edit-update`：password 输入框，占位符 `••••••` 表示"当前可能有值，不动即保留"；非必填时右侧常显"清空"按钮 → emit `""`。初始 `value` 为 `null`（详情不返回该字段）。
> - `edit` + 非 edit-update（即创建）：维持原行为（`clearable` + `请输入`）。
> - `search` 模式在 field-policy 中已被排除，不会渲染。

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/fields/SecretField.vue
git commit -m "feat: render secret field with placeholder and clear button on edit"
```

---

## Task 11: 文档 — 更新 form-generator/CLAUDE.md 的 secret 描述

**Files:**
- Modify: `form-generator/CLAUDE.md`（`### secret` 段，"无额外字段。列表/搜索时脱敏..."）

- [ ] **Step 1: 更新 secret 类型描述**

当前：

```markdown
### secret

无额外字段。列表/搜索时脱敏，详情时明文，编辑时只能重置不能修改。
```

改为（修正过时描述——详情不返回明文；编辑支持"不改即保留"的可选覆盖）：

```markdown
### secret

无额外字段。列表/搜索时不返回、详情接口不返回明文（安全）。编辑（canInputOnEdit=true）时采用可选覆盖协议：
EditReqDTO 不带 `@NotEmpty`；提交 `null`=不修改（保留原值）、`""`=清空（isNonVoid=false 存空串，
isNonVoid=true 抛业务异常）、非空串=覆盖。canInputOnEdit=false 时编辑弹框隐藏该字段。
```

- [ ] **Step 2: Commit**

```bash
git add form-generator/CLAUDE.md
git commit -m "docs: update secret field description for edit-echo protocol"
```

---

## Task 12: 全量验证

- [ ] **Step 1: 运行 form-generator 全部 IT**

Run: `mvn test -pl allison1875-cli -am -Dtest="com.spldeolin.allison1875.cli.it.formgenerator.*"`
Expected: 全部 PASS（含新 SecretEditEchoItTest，且未破坏 CannotInputOnInitItTest、BasicFormItTest 等）

- [ ] **Step 2: 全量 verify（可选，确认无回归）**

Run: `mvn verify`
Expected: BUILD SUCCESS

- [ ] **Step 3: 人工审查前端 4 个文件**

确认：field-policy 仅对 secret 新增隐藏；EditModal 仅对 edit-update 的 secret 免必填；FieldRenderer 仅 secret 透传 editMode；SecretField 三分支渲染正确，create 行为未变。

---

## Self-Review 结果

- **Spec coverage：** 后端改动 1（Task 1）、改动 2b/2（Task 2、5、6）、前端改动 3-6（Task 7-10）、测试（Task 3-4、12）、文档（Task 11）——全部覆盖。`request-builder.ts`/`response-parser.ts` 按 spec 明确不改，无对应任务（正确）。
- **Placeholder scan：** 无 TBD/TODO；每个代码步骤含完整代码。已核实 SelectItemService 的 StringUtils 边界（仅被删方法使用），Task 6 Step 3 为确定性删除指令。
- **Type consistency：** `editMode: 'edit-create' | 'edit-update'` 在 EditModal（透传）、FieldRenderer（prop）、SecretField（prop）三处签名一致；`generateSecretNullSkipSetter` 签名 `(FormDef, ItemDef, BlockStmt)` 与调用点一致；DTO/Service 文件名 `Update/CreateCredentialReq`、`UpdateCredentialServiceImpl`、`GetCredentialDetailResp` 与 form name `Credential` 一致。
