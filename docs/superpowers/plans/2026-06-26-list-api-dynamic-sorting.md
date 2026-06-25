# List API 动态排序 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable form-generator's list API to support dynamic sorting via request parameters, with a new `MapperLayerExpansionService` in query-transformer as the extension mechanism.

**Architecture:** New `MapperLayerExpansionService` interface in query-transformer provides two extension points (mapper params + ORDER BY XML). form-generator overrides via `Modules.override()` to inject sort enum fields into ParamDTO and generate dynamic `<choose>` ORDER BY XML. The sort enum is generated per form.

**Tech Stack:** Java 21, JavaParser AST, Google Guice DI, MyBatis XML generation

---

## File Structure

| Action | Path | Responsibility |
|--------|------|---------------|
| Create | `query-transformer/src/main/java/.../querytransformer/service/MapperLayerExpansionService.java` | Extension interface |
| Create | `query-transformer/src/main/java/.../querytransformer/service/impl/DefaultMapperLayerExpansionServiceImpl.java` | No-op default impl |
| Create | `query-transformer/src/main/java/.../querytransformer/dto/ExpandParamRetval.java` | Return DTO for expandParam |
| Create | `query-transformer/src/main/java/.../querytransformer/dto/ExpandedFieldDTO.java` | Individual field descriptor |
| Modify | `query-transformer/src/main/java/.../querytransformer/dto/GenerateParamRetval.java` | Add `expandParamRetval` field |
| Modify | `query-transformer/src/main/java/.../querytransformer/service/impl/MethodGeneratorServiceImpl.java` | Integrate expansion into param generation |
| Modify | `query-transformer/src/main/java/.../querytransformer/service/impl/MapperLayerServiceImpl.java` | Integrate expansion into XML ORDER BY |
| Modify | `query-transformer/src/main/java/.../querytransformer/service/impl/TransformMethodCallServiceImpl.java` | Generate expanded field setter stmts |
| Modify | `form-generator/src/main/java/.../formgenerator/service/impl/EnumServiceImpl.java` | Enable sort enum generation + filter by type |
| Modify | `form-generator/src/main/java/.../formgenerator/service/impl/ListApiServiceImpl.java` | Add sortBy/isAsc fields + remove .order() |
| Create | `form-generator/src/main/java/.../formgenerator/service/impl/FormGeneratorMapperLayerExpansionServiceImpl.java` | Expansion impl with sort logic |
| Modify | `form-generator/src/main/java/.../formgenerator/FormGeneratorModule.java` | Bind expansion override |

---

### Task 1: Create ExpandedFieldDTO and ExpandParamRetval DTOs

**Files:**
- Create: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/ExpandedFieldDTO.java`
- Create: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/ExpandParamRetval.java`

- [ ] **Step 1: Create ExpandedFieldDTO**

```java
package com.spldeolin.allison1875.querytransformer.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpandedFieldDTO {

    String typeQualifier;

    String fieldName;

    String description;

    String sourceExpression;

}
```

- [ ] **Step 2: Create ExpandParamRetval**

```java
package com.spldeolin.allison1875.querytransformer.dto;

import java.util.List;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpandParamRetval {

    List<ExpandedFieldDTO> expandedFields = Lists.newArrayList();

}
```

- [ ] **Step 3: Add expandParamRetval field to GenerateParamRetval**

Modify `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/GenerateParamRetval.java`:

Add field after `Boolean isParamDTO;`:
```java
ExpandParamRetval expandParamRetval;
```

- [ ] **Step 4: Compile to verify**

Run: `mvn compile -pl query-transformer -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/ExpandedFieldDTO.java \
       query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/ExpandParamRetval.java \
       query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/dto/GenerateParamRetval.java
git commit -m "feat: add ExpandedFieldDTO and ExpandParamRetval DTOs for mapper layer expansion"
```

---

### Task 2: Create MapperLayerExpansionService interface and default implementation

**Files:**
- Create: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/MapperLayerExpansionService.java`
- Create: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/DefaultMapperLayerExpansionServiceImpl.java`

- [ ] **Step 1: Create MapperLayerExpansionService interface**

```java
package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.DefaultMapperLayerExpansionServiceImpl;

/**
 * @author Deolin 2026-06-26
 */
@ImplementedBy(DefaultMapperLayerExpansionServiceImpl.class)
public interface MapperLayerExpansionService {

    ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis);

    List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin);

}
```

- [ ] **Step 2: Create DefaultMapperLayerExpansionServiceImpl**

```java
package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Collections;
import java.util.List;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-26
 */
@Singleton
@Slf4j
public class DefaultMapperLayerExpansionServiceImpl implements MapperLayerExpansionService {

    @Override
    public ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis) {
        return new ExpandParamRetval();
    }

    @Override
    public List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin) {
        return Collections.emptyList();
    }

}
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -pl query-transformer -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/MapperLayerExpansionService.java \
       query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/DefaultMapperLayerExpansionServiceImpl.java
git commit -m "feat: add MapperLayerExpansionService interface with no-op default"
```

---

### Task 3: Integrate MapperLayerExpansionService into MethodGeneratorServiceImpl

**Files:**
- Modify: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/MethodGeneratorServiceImpl.java`

- [ ] **Step 1: Add injection field**

Add after the existing `@Inject private DataModelService dataModelGeneratorService;` field (around line 49):

```java
@Inject
private MapperLayerExpansionService mapperLayerExpansionService;
```

Add import:
```java
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
```

- [ ] **Step 2: Modify generateParam method**

At the beginning of `generateParam` (after line 56 `Set<Binary> binaries = chainAnalysis.getBinariesAsArgs();`), call the expansion service:

```java
ExpandParamRetval expandParamRetval = mapperLayerExpansionService.expandParam(chainAnalysis);
List<ExpandedFieldDTO> expandedFields = expandParamRetval.getExpandedFields();
int totalFieldCount = binaries.size() + expandedFields.size();
```

Replace the existing threshold check on line 57:
```java
if (binaries.size() > 3 || (binaries.size() > 1 && chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE)) {
```

With:
```java
if (totalFieldCount > 3 || (totalFieldCount > 1 && chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE)) {
```

- [ ] **Step 3: Add expanded fields to ParamDTO branch**

In the ParamDTO branch (the first `if` block), after the pagination fields are added (after line 82 `dataModelArg.getFieldArgs().add(new FieldArg()...limit...)`), add expanded fields:

```java
for (ExpandedFieldDTO expandedField : expandedFields) {
    FieldArg fieldArg = new FieldArg();
    fieldArg.setDescription(expandedField.getDescription());
    fieldArg.setTypeQualifier(expandedField.getTypeQualifier());
    fieldArg.setFieldName(expandedField.getFieldName());
    dataModelArg.getFieldArgs().add(fieldArg);
}
```

- [ ] **Step 4: Add expanded fields to non-ParamDTO branch**

In the `else if (CollectionUtils.isNotEmpty(binaries))` branch (line 90), after pagination params are added (after line 109 `params.add(new Parameter().setType("Integer").setName("limit"));`), add:

```java
for (ExpandedFieldDTO expandedField : expandedFields) {
    Parameter param = new Parameter();
    param.addAnnotation(parseAnnotation(
            String.format("@org.apache.ibatis.annotations.Param(\"%s\")", expandedField.getFieldName())));
    param.setType(expandedField.getTypeQualifier());
    param.setName(expandedField.getFieldName());
    params.add(param);
}
```

- [ ] **Step 5: Handle the empty-binaries branch**

In the `else` branch (line 112, where `binaries` is empty but expanded fields might exist), the current logic returns early with only pagination params. Now we need to handle the case where `expandedFields` is non-empty. Replace the entire `else` block:

```java
} else {
    if (CollectionUtils.isNotEmpty(expandedFields)) {
        if (expandedFields.size() > 3 || chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
            DataModelArg dataModelArg = new DataModelArg();
            dataModelArg.setSourceRoot(DomainContext.get().getPersistenceSourceRoot());
            dataModelArg.setPackageName(DomainContext.get().getParamDTOPackage());
            dataModelArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Param");
            dataModelArg.setAuthor(config.getAuthor());
            for (ExpandedFieldDTO expandedField : expandedFields) {
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(expandedField.getDescription());
                fieldArg.setTypeQualifier(expandedField.getTypeQualifier());
                fieldArg.setFieldName(expandedField.getFieldName());
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                dataModelArg.getFieldArgs()
                        .add(new FieldArg().setTypeQualifier("java.lang.Integer").setFieldName("offset"));
                dataModelArg.getFieldArgs()
                        .add(new FieldArg().setTypeQualifier("java.lang.Integer").setFieldName("limit"));
            }
            dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
            DataModelGeneration paramDTOGeneration = dataModelGeneratorService.generateDataModel(dataModelArg);
            Parameter param = new Parameter();
            param.setType(paramDTOGeneration.getDtoQualifier());
            param.setName(MoreStringUtils.toLowerCamel(paramDTOGeneration.getDtoName()));
            params.add(param);
            isParamDTO = true;
        } else {
            for (ExpandedFieldDTO expandedField : expandedFields) {
                Parameter param = new Parameter();
                param.addAnnotation(parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", expandedField.getFieldName())));
                param.setType(expandedField.getTypeQualifier());
                param.setName(expandedField.getFieldName());
                params.add(param);
            }
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                params.add(new Parameter().setType("Integer").setName("offset"));
                params.add(new Parameter().setType("Integer").setName("limit"));
            }
        }
    } else {
        GenerateParamRetval retval = new GenerateParamRetval();
        retval.setIsParamDTO(false);
        retval.setExpandParamRetval(expandParamRetval);
        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
            retval.getParameters().add(new Parameter().setType("Integer").setName("offset"));
            retval.getParameters().add(new Parameter().setType("Integer").setName("limit"));
        }
        return retval;
    }
}
```

- [ ] **Step 6: Set expandParamRetval on the result**

Before the existing `return result;` at the end of the method (line 124), add:

```java
result.setExpandParamRetval(expandParamRetval);
```

- [ ] **Step 7: Compile to verify**

Run: `mvn compile -pl query-transformer -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/MethodGeneratorServiceImpl.java
git commit -m "feat: integrate MapperLayerExpansionService into MethodGeneratorServiceImpl.generateParam"
```

---

### Task 4: Integrate MapperLayerExpansionService into MapperLayerServiceImpl (ORDER BY)

**Files:**
- Modify: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/MapperLayerServiceImpl.java`

- [ ] **Step 1: Add injection field**

Add field:
```java
@Inject
private MapperLayerExpansionService mapperLayerExpansionService;
```

Add imports:
```java
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
```

- [ ] **Step 2: Add expansion ORDER BY lines after existing ORDER BY logic**

In `generateMethodToMapperXml`, after the existing ORDER BY block (after line 217, right after `xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));` and its closing `}`), add:

```java
// expansion order by部分
List<String> expansionOrderByLines = mapperLayerExpansionService.expandOrderByLines(
        chainAnalysis, designMeta, join);
if (CollectionUtils.isNotEmpty(expansionOrderByLines)) {
    for (String line : expansionOrderByLines) {
        xmlLines.add(SINGLE_INDENT + line);
    }
}
```

Add import for `java.util.List` if not present (it likely already is).

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -pl query-transformer -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/MapperLayerServiceImpl.java
git commit -m "feat: integrate MapperLayerExpansionService ORDER BY into MapperLayerServiceImpl"
```

---

### Task 5: Integrate expansion into TransformMethodCallServiceImpl (chain replacer)

**Files:**
- Modify: `query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/TransformMethodCallServiceImpl.java`

- [ ] **Step 1: Add imports**

```java
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
```

- [ ] **Step 2: Modify argumentBuildStmts to include expanded field setters**

After the pagination setters (after line 65, `paramDTOVarName + ".setLimit(" + chainAnalysis.getLimitExpr() + ");"` block), add:

```java
if (paramGeneration.getExpandParamRetval() != null) {
    for (ExpandedFieldDTO expandedField : paramGeneration.getExpandParamRetval().getExpandedFields()) {
        result.add(parseStatement(
                paramDTOVarName + ".set" + MoreStringUtils.toUpperCamel(expandedField.getFieldName()) + "("
                        + expandedField.getSourceExpression() + ");"));
    }
}
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -pl query-transformer -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add query-transformer/src/main/java/com/spldeolin/allison1875/querytransformer/service/impl/TransformMethodCallServiceImpl.java
git commit -m "feat: generate expanded field setter statements in chain replacer"
```

---

### Task 6: Modify EnumServiceImpl to enable sort enum generation

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/EnumServiceImpl.java`

- [ ] **Step 1: Change enum name from SortItemEnum to SortEnum**

Replace line 108:
```java
String enumName = StringUtils.capitalize(form.getName()) + "SortItemEnum";
```
with:
```java
String enumName = StringUtils.capitalize(form.getName()) + "SortEnum";
```

- [ ] **Step 2: Filter enum items by sortable types**

Replace lines 126-132 (the `for` loop that iterates all items):
```java
for (ItemDef item : form.getItems()) {
    EnumConstantDeclaration ecd = new EnumConstantDeclaration().setName(
                    MoreStringUtils.camelToSnakeCase(item.getName()).toUpperCase())
            .addArgument(new StringLiteralExpr(item.getName()))
            .addArgument(new StringLiteralExpr("按"" + item.getTitle() + ""排序"));
    ed.addEntry(ecd);
}
```

with:
```java
for (ItemDef item : form.getItems()) {
    if (item.getType() != ItemType.NUMBER && item.getType() != ItemType.ON_OFF
            && item.getType() != ItemType.TEXT && item.getType() != ItemType.TIME) {
        continue;
    }
    EnumConstantDeclaration ecd = new EnumConstantDeclaration().setName(
                    MoreStringUtils.camelToSnakeCase(item.getName()).toUpperCase())
            .addArgument(new StringLiteralExpr(item.getName()))
            .addArgument(new StringLiteralExpr("按"" + item.getTitle() + ""排序"));
    ed.addEntry(ecd);
}
// createdAt, updatedAt
ed.addEntry(new EnumConstantDeclaration().setName("CREATED_AT")
        .addArgument(new StringLiteralExpr("createdAt"))
        .addArgument(new StringLiteralExpr("按"创建时间"排序")));
ed.addEntry(new EnumConstantDeclaration().setName("UPDATED_AT")
        .addArgument(new StringLiteralExpr("updatedAt"))
        .addArgument(new StringLiteralExpr("按"更新时间"排序")));
```

- [ ] **Step 3: Uncomment writeJava call**

Replace line 153:
```java
//            CompilationUnitUtils.writeJava(cu); TODO query-transformer能力不支持，所以暂时固定为更新时间倒序，不生成排序字段枚举
```
with:
```java
CompilationUnitUtils.writeJava(cu);
```

- [ ] **Step 4: Compile to verify**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/EnumServiceImpl.java
git commit -m "feat: enable sort enum generation with type filtering"
```

---

### Task 7: Modify ListApiServiceImpl to add sortBy/isAsc fields and remove .order()

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/ListApiServiceImpl.java`

- [ ] **Step 1: Uncomment and adjust sort fields in generateListInitDec**

Replace lines 154-162 (the commented-out sort section):
```java
        // 排序方式 TODO query-transformer能力不支持，所以暂时固定为更新时间倒序
//        itemField = StaticJavaParserUtils.parseFieldDeclaration(form.getName() + "SortItemEnum sortItem;")
//                .asFieldDeclaration();
//        JavadocUtils.setJavadoc(itemField, "排序字段，null代表更新时间", null);
//        reqCoid.addMember(itemField);
//        itemField = StaticJavaParserUtils.parseFieldDeclaration("Boolean isSortAsc;")
//                .asFieldDeclaration();
//        JavadocUtils.setJavadoc(itemField, "true代表正序，否则代表倒序", null);
//        reqCoid.addMember(itemField);
```

with:
```java
        // 排序方式
        itemField = parseFieldDeclaration(form.getName() + "SortEnum sortBy;");
        JavadocUtils.setJavadoc(itemField, "排序字段，null代表更新时间倒序", null);
        reqCoid.addMember(itemField);
        itemField = parseFieldDeclaration("Boolean isAsc;");
        JavadocUtils.setJavadoc(itemField, "true代表正序，否则代表倒序", null);
        reqCoid.addMember(itemField);
```

- [ ] **Step 2: Remove hardcoded .order() in generateMethodBody**

Replace line 195:
```java
        designChain += ".order().updatedAt.desc()"; // TODO query-transformer能力不支持，所以暂时固定为更新时间倒序
```

with (delete the line entirely, no replacement — the next line `designChain += ".page(..."` remains):
```java
```

That is, simply remove line 195 so the code goes directly from the search conditions loop to `.page(...)`.

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/ListApiServiceImpl.java
git commit -m "feat: add sortBy/isAsc to list ReqDTO and remove hardcoded .order()"
```

---

### Task 8: Create FormGeneratorMapperLayerExpansionServiceImpl

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorMapperLayerExpansionServiceImpl.java`

- [ ] **Step 1: Create the implementation class**

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-26
 */
@Slf4j
public class FormGeneratorMapperLayerExpansionServiceImpl implements MapperLayerExpansionService {

    private List<ItemDef> sortableItems = Collections.emptyList();

    public void setSortableItems(List<ItemDef> sortableItems) {
        this.sortableItems = sortableItems;
    }

    @Override
    public ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis) {
        ExpandParamRetval retval = new ExpandParamRetval();
        retval.getExpandedFields().add(new ExpandedFieldDTO()
                .setTypeQualifier("java.lang.String")
                .setFieldName("sortBy")
                .setDescription("排序字段")
                .setSourceExpression("req.getSortBy() != null ? req.getSortBy().getCode() : null"));
        retval.getExpandedFields().add(new ExpandedFieldDTO()
                .setTypeQualifier("java.lang.Boolean")
                .setFieldName("isAsc")
                .setDescription("是否正序")
                .setSourceExpression("req.getIsAsc()"));
        return retval;
    }

    @Override
    public List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin) {
        Map<String, PropertyDTO> properties = designMeta.getProperties();
        List<String> lines = Lists.newArrayList();
        lines.add("<choose>");
        lines.add("  <when test=\"sortBy != null\">");

        // build inner choose for column mapping
        lines.add("    ORDER BY");
        lines.add("    <choose>");
        for (ItemDef item : sortableItems) {
            PropertyDTO property = properties.get(item.getName());
            if (property == null) {
                continue;
            }
            String columnName = (isJoin ? "t1." : "") + property.getColumnName();
            lines.add("      <when test=\"sortBy == '" + item.getName() + "'\">" + columnName + "</when>");
        }
        // createdAt, updatedAt
        PropertyDTO createdAtProp = properties.get("createdAt");
        if (createdAtProp != null) {
            String col = (isJoin ? "t1." : "") + createdAtProp.getColumnName();
            lines.add("      <when test=\"sortBy == 'createdAt'\">" + col + "</when>");
        }
        PropertyDTO updatedAtProp = properties.get("updatedAt");
        if (updatedAtProp != null) {
            String col = (isJoin ? "t1." : "") + updatedAtProp.getColumnName();
            lines.add("      <when test=\"sortBy == 'updatedAt'\">" + col + "</when>");
        }
        lines.add("      <otherwise>" + (isJoin ? "t1." : "") + "id</otherwise>");
        lines.add("    </choose>");

        // ASC/DESC
        lines.add("    <choose>");
        lines.add("      <when test=\"isAsc != null and isAsc\">ASC</when>");
        lines.add("      <otherwise>DESC</otherwise>");
        lines.add("    </choose>");

        lines.add("  </when>");
        lines.add("  <otherwise>");
        lines.add("    ORDER BY " + (isJoin ? "t1." : "") + "id DESC");
        lines.add("  </otherwise>");
        lines.add("</choose>");
        return lines;
    }

}
```

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorMapperLayerExpansionServiceImpl.java
git commit -m "feat: add FormGeneratorMapperLayerExpansionServiceImpl with sort logic"
```

---

### Task 9: Bind expansion override in FormGeneratorModule and set sortable items in FormGenerator

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGeneratorModule.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java`

- [ ] **Step 1: Modify FormGeneratorModule to bind MapperLayerExpansionService**

Add imports:
```java
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMapperLayerExpansionServiceImpl;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
```

In the `configure()` method, in the inner `AbstractModule`, after the `bind(ServiceLayerExpansionService.class)` line (line 45-46), add:

```java
bind(MapperLayerExpansionService.class)
        .toInstance(new FormGeneratorMapperLayerExpansionServiceImpl());
```

- [ ] **Step 2: Modify FormGenerator to set sortable items before queryTransformer.play()**

Add import:
```java
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMapperLayerExpansionServiceImpl;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType.*;
```

Add injection field:
```java
@Inject
private MapperLayerExpansionService mapperLayerExpansionService;
```

Before line 168 (`queryTransformer.play();`), add code to set sortable items for the current forms. Since form-generator processes all forms at once and `queryTransformer.play()` processes all Design chains in one go, we need to set a combined sortable item list. However, the expansion service is per-query-chain and there's only one list API per form processed sequentially. The simplest approach: set all sortable items (union of all forms) since the column mapping will be resolved per design meta.

Add before `queryTransformer.play();`:
```java
if (mapperLayerExpansionService instanceof FormGeneratorMapperLayerExpansionServiceImpl) {
    List<ItemDef> allSortableItems = Lists.newArrayList();
    for (FormDef form : forms) {
        for (ItemDef item : form.getItems()) {
            if (item.getType() == NUMBER || item.getType() == ON_OFF
                    || item.getType() == TEXT || item.getType() == TIME) {
                allSortableItems.add(item);
            }
        }
    }
    ((FormGeneratorMapperLayerExpansionServiceImpl) mapperLayerExpansionService).setSortableItems(allSortableItems);
}
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGeneratorModule.java \
       form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java
git commit -m "feat: bind MapperLayerExpansionService override and set sortable items"
```

---

### Task 10: Run full test suite and fix issues

**Files:**
- Various files for fixes if needed

- [ ] **Step 1: Run full verify**

Run: `mvn verify -q`
Expected: BUILD SUCCESS with all tests passing

- [ ] **Step 2: Run form-generator specific IT tests**

Run: `mvn test -pl allison1875-cli -am -Dtest="*FormGenerator*ItTest" -q`

If there are no form-generator specific tests matching that pattern, try:
Run: `mvn test -pl allison1875-cli -am -q`

- [ ] **Step 3: Fix any compilation or test failures**

Address issues found. Common issues to check:
- Missing imports in modified files
- `ExpandParamRetval` might be null in existing code paths — ensure `generateParamRetval.getExpandParamRetval()` is null-safe in `TransformMethodCallServiceImpl`
- The `else` branch refactoring in `MethodGeneratorServiceImpl` might need the `params` and `isParamDTO` variables to flow correctly into the tail return

- [ ] **Step 4: Commit fixes if any**

```bash
git add -A
git commit -m "fix: address test failures from dynamic sorting integration"
```
