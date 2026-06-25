# List API 动态排序支持

## 概述

为 form-generator 生成的 list 接口添加动态排序能力。用户请求可通过 `sortBy`（排序字段枚举）和 `isAsc`（排序方向）参数控制查询排序，未指定时默认按 `id DESC`。

通过在 query-transformer 中新增 `MapperLayerExpansionService` 扩展点，form-generator 注入自己的实现来完成排序参数的传递与 SQL 生成。

## 变更范围

| 模块 | 变更内容 |
|------|---------|
| query-transformer | 新增 `MapperLayerExpansionService` 接口 + 空默认实现 |
| query-transformer | `MethodGeneratorServiceImpl.generateParam()` 集成扩展点 |
| query-transformer | `MapperLayerServiceImpl.generateMethodToMapperXml()` 集成扩展点 |
| query-transformer | query-transformer 的 chain replacer 集成扩展点（生成 ParamDTO 赋值语句） |
| form-generator | `EnumServiceImpl` 取消注释并调整 SortEnum 生成逻辑 |
| form-generator | `ListApiServiceImpl` 新增 ReqDTO 排序字段 + 移除硬编码 `.order()` |
| form-generator | 新增 `FormGeneratorMapperLayerExpansionServiceImpl` 覆盖实现 |
| form-generator | `FormGeneratorModule` 绑定覆盖实现 |

## 详细设计

### 1. 排序枚举生成

**位置**：`form-generator` → `EnumServiceImpl`

取消注释现有的排序枚举生成逻辑，调整如下：

- **枚举类名**：`{FormName}SortEnum`（原来是 `{FormName}SortItemEnum`）
- **包路径**：`DomainContext.get().getEnumPackage()`
- **枚举项来源**：
  - 所有 type 为 `number`、`onOff`、`text`、`time` 的 FormItem
  - 自动追加的 `createdAt`、`updatedAt`
  - 在 hasUserForm 分支中，额外追加 `createdBy`、`updatedBy`
- **常量命名**：`MoreStringUtils.camelToSnakeCase(propertyName).toUpperCase()`
- **构造参数**：`code`（即 propertyName，如 `"amount"`）+ `title`（如 `"按金额排序"`）
- **标准方法**：`@JsonValue` 在 code 字段、`valid(String code)`、`@JsonCreator of(String code)`

**示例**（对于 Order form，含 orderNo(text)、amount(number)、isPaid(onOff)、paidAt(time) 字段）：

```java
@Getter
@AllArgsConstructor
public enum OrderSortEnum {

    ORDER_NO("orderNo", "按订单号排序"),
    AMOUNT("amount", "按金额排序"),
    IS_PAID("isPaid", "按是否已付款排序"),
    PAID_AT("paidAt", "按付款时间排序"),
    CREATED_AT("createdAt", "按创建时间排序"),
    UPDATED_AT("updatedAt", "按更新时间排序");

    @JsonValue
    private final String code;
    private final String title;

    public static boolean valid(String code) { ... }
    @JsonCreator public static OrderSortEnum of(String code) { ... }
}
```

### 2. List ReqDTO 新增字段

**位置**：`form-generator` → `ListApiServiceImpl.generateInitDec()`

在分页参数（pageNum、pageSize）之后追加：

```java
{FormName}SortEnum sortBy;   // Javadoc: "排序字段，null代表更新时间倒序"
Boolean isAsc;               // Javadoc: "true代表正序，否则代表倒序"
```

不加 `@NotNull`，允许为空（空时走默认排序）。

### 3. 移除硬编码 `.order()` 调用

**位置**：`form-generator` → `ListApiServiceImpl.generateMethodBody()`

移除：
```java
designChain += ".order().updatedAt.desc()";
```

Design chain 不再包含静态排序子句，排序完全由 MapperLayerExpansionService 在 XML 层处理。

### 4. MapperLayerExpansionService 接口

**位置**：`query-transformer` → `com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService`

```java
@ImplementedBy(DefaultMapperLayerExpansionServiceImpl.class)
public interface MapperLayerExpansionService {

    ExpandParamRetval expandParam(ChainAnalysisDTO chainAnalysis);

    List<String> expandOrderByLines(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta, boolean isJoin);

}
```

### 5. ExpandParamRetval DTO

**位置**：`query-transformer` → `com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval`

```java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpandParamRetval {

    List<ExpandedFieldDTO> expandedFields = Lists.newArrayList();

}
```

```java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpandedFieldDTO {

    String typeQualifier;    // 字段类型全限定名，如 "java.lang.String"

    String fieldName;        // 字段名，如 "sortBy"

    String description;      // 字段描述

    String sourceExpression; // 在 service 层中的源表达式，如 "req.getSortBy() != null ? req.getSortBy().getCode() : null"

}
```

`expandedFields.size()` 参与 `generateParam` 中 isParamDTO 的阈值判断。

### 6. 默认空实现

**位置**：`query-transformer` → `com.spldeolin.allison1875.querytransformer.service.impl.DefaultMapperLayerExpansionServiceImpl`

```java
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

### 7. MethodGeneratorServiceImpl 集成

**位置**：`query-transformer` → `MethodGeneratorServiceImpl.generateParam()`

改动要点：

1. 注入 `MapperLayerExpansionService`
2. 调用 `expansionService.expandParam(chainAnalysis)` 获取 `ExpandParamRetval`
3. 将 `expandedFields.size()` 加入 binaries 大小判断：`binaries.size() + expandedFields.size() > 3` 决定是否生成 ParamDTO
4. 若生成 ParamDTO，将 expandedFields 中的每个字段作为 `FieldArg` 加入 `dataModelArg`
5. 若不生成 ParamDTO（走 @Param 独立参数），将 expandedFields 作为独立 `Parameter` 追加
6. 将 `ExpandParamRetval` 存入 `GenerateParamRetval` 中（新增字段），供 chain replacer 使用

`GenerateParamRetval` 新增字段：
```java
ExpandParamRetval expandParamRetval;
```

### 8. MapperLayerServiceImpl 集成

**位置**：`query-transformer` → `MapperLayerServiceImpl.generateMethodToMapperXml()`

在 ORDER BY 部分之后（或替代现有 ORDER BY 生成逻辑）：

1. 先处理 `chainAnalysis.getSortProperties()`（来自 DSL 的静态排序）
2. 调用 `expansionService.expandOrderByLines(chainAnalysis, designMeta, join)` 获取额外 XML 行
3. 将额外行追加到 xmlLines

当静态排序和动态排序同时存在时，静态排序在前，动态排序在后。当只有动态排序时（如 form-generator 场景），仅输出动态排序 XML。

### 9. Chain Replacer 集成

**位置**：query-transformer 中负责将 Design chain 替换为 mapper 调用的服务

当生成 ParamDTO 的 setter 赋值语句时，除了 binaries 对应的字段，还需遍历 `GenerateParamRetval.expandParamRetval.expandedFields`，为每个 expandedField 生成：

```java
param.setSortBy(req.getSortBy() != null ? req.getSortBy().getCode() : null);
param.setIsAsc(req.getIsAsc());
```

其中 setter 调用的参数值来自 `ExpandedFieldDTO.sourceExpression`。

### 10. form-generator 覆盖实现

**位置**：`form-generator` → `com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMapperLayerExpansionServiceImpl`

#### expandParam

返回 2 个 expandedField：

| typeQualifier | fieldName | description | sourceExpression |
|---|---|---|---|
| `java.lang.String` | `sortBy` | 排序字段 | `req.getSortBy() != null ? req.getSortBy().getCode() : null` |
| `java.lang.Boolean` | `isAsc` | 是否正序 | `req.getIsAsc()` |

#### expandOrderByLines

生成 MyBatis XML 动态排序片段：

```xml
<choose>
    <when test="sortBy != null">
        <choose>
            <when test="sortBy == 'orderNo'">ORDER BY order_no</when>
            <when test="sortBy == 'amount'">ORDER BY amount</when>
            <when test="sortBy == 'createdAt'">ORDER BY created_at</when>
            <when test="sortBy == 'updatedAt'">ORDER BY updated_at</when>
        </choose>
        <if test="isAsc != null and isAsc">ASC</if>
        <if test="isAsc == null or !isAsc">DESC</if>
    </when>
    <otherwise>
        ORDER BY id DESC
    </otherwise>
</choose>
```

**实现细节**：该方法需要访问当前 form 的排序字段列表。由于 ExpansionService 的方法签名接收 `ChainAnalysisDTO` 和 `DesignMetaDTO`，实现类需要通过 `designMeta.getProperties()` 获取列名映射（propertyName → columnName）。排序字段列表由实现类内部维护（通过注入 form-generator 的上下文或在 FormGenerator 主流程中设置到 ThreadLocal/实例状态）。

### 11. FormGeneratorModule 绑定

**位置**：`form-generator` → `FormGeneratorModule.configure()`

在现有 `Modules.override()` 的 `AbstractModule` 中追加：

```java
bind(MapperLayerExpansionService.class)
        .toInstance(new FormGeneratorMapperLayerExpansionServiceImpl(...));
```

### 12. 排序字段列表传递机制

`FormGeneratorMapperLayerExpansionServiceImpl` 需要知道当前 form 的可排序字段列表（propertyName → columnName 映射）。

方案：该实现类持有一个 `setSortableItems(List<ItemDef> items)` 方法。`FormGenerator` 主流程在处理每个 form 并调用 `QueryTransformer.play()` 之前，调用此 setter 设置当前 form 的可排序字段。由于 `FormGeneratorModule` 中用 `toInstance()` 绑定，该实例在整个 form-generator 生命周期内是同一个对象，可以安全地在处理每个 form 前 set 当前上下文。

## 数据流

```
用户请求 { sortBy: "amount", isAsc: true }
    ↓
Controller → Service (form-generator 生成)
    ↓ req.getSortBy().getCode() → "amount", req.getIsAsc() → true
Mapper ParamDTO { sortBy: "amount", isAsc: true, ...其他查询条件 }
    ↓
Mapper XML <choose> 分支匹配 "amount" → ORDER BY amount ASC
    ↓
SQL: SELECT ... FROM t_order WHERE ... ORDER BY amount ASC LIMIT ?, ?
```

## 测试策略

在 `allison1875-cli` 中新增或扩展现有 form-generator IT 用例，验证：
1. 排序枚举正确生成到 enumPackage
2. List ReqDTO 包含 sortBy 和 isAsc 字段
3. Mapper XML 包含动态排序 `<choose>` 片段
4. ParamDTO 包含 sortBy 和 isAsc 字段
5. Service 层代码正确传递排序参数到 mapper 调用
