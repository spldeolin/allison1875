# star-transformer 集成测试用例说明

本目录包含 star-transformer 工具的全部集成测试用例。每个测试通过基类
`StarTransformerItBaseTest` 执行三阶段流程：

1. 先运行 **persistence-generator**，根据 `.allison1875.yml` 中的 DDL 生成 Entity / Design /
   Mapper / XML（star-transformer 依赖 `XxxDesign.id` 等 `PropertyName<E,K>` 作为 DSL 入参）。
2. 将 `src/main/java-st/` 下含 **StarChain DSL** 的 Service 源码拷贝到 `src/main/java/`
   （模拟原 invoker 模式中 maven-resources-plugin 的 copy-st-sources execution）。
3. 运行 **star-transformer**，解析以 `StarSchema.cft(...).oo(...).om(...).key(...).mkey(...).over()`
   收尾的星型链，生成 `XxxWholeDTO` 数据装配 DTO，并把原链所在语句替换为一组
   `query().byForced().id.eq(...)` / `fk.eq(...)` 调用 + Map / Multimap 声明 + foreach
   put + `whole.setXxx(...)` 调用。

断言主要覆盖：

- 是否生成了正确包名/类名的 **WholeDTO** 文件，字段类型与名称是否符合预期；
- 原 Service 文件中 `StarSchema.` / `.over()` 链是否消失，并被展开为多条标准化查询/装配语句；
- 是否正确引入 `LinkedHashMap`、`com.google.common.collect.LinkedListMultimap` /
  `Multimap`、Entity / Design 等导入；
- 复数化命名（`English.plural`）、setter 名（`CodeGenerationUtils.setterName`）是否正确。

star-transformer 的核心职责：

> **将一段以 `StarSchema.cft(事实表PK, 主键值).oo(维度表FK).om(维度表FK).key(维度表key).mkey(...).over()`
> 描述的"星型 schema"DSL，转换为可执行的多表分次查询 + 内存装配代码，并产出一个聚合 DTO
> （WholeDTO）来承载装配结果。**

---

## 用例清单（共 7 个）

为减少重复 fixture，相近分支以"同 Service 内多方法/多链 + 一次性多重断言"的方式合并到同一用例。

### 1. CftOnlyItTest

**目标：** 最小可用链 + WholeDTO 命名常规分支（cft Entity 以 `Entity` 结尾，走
`replaceLast("Entity", postfix)` 分支）。

**DSL：** 单方法、单链：`StarSchema.cft(TOrderDesign.id, orderId).over();`

**断言：**

- 生成 `com/example/dto/whole/TOrderWholeDTO.java`；类型仅含 `TOrderEntity tOrder` 字段。
- Service 替换后包含：
    - `TOrderWholeDTO whole = new TOrderWholeDTO();`
    - `TOrderEntity tOrder = TOrderDesign.query().byForced().id.eq(orderId).one();`
    - `whole.setTOrder(tOrder);`
- 原 `StarSchema.cft(...)...over();` 语句已消失。

### 2. NotStarChainNoTransformItTest（负向）

**目标：** 验证 `detectStarChains` → `finalNameExprRecursively` 的 StarSchema 根过滤；
同方法内非法 `over()` 链与合法星型链共存时互不影响。

**DSL：** 同方法包含三条语句：

- `someBuilder.foo().over();`（NameExpr scope，非 StarSchema）→ 不应被改写
- `unknownVar.over();`（无解析类型 → 异常分支返回 false）→ 不应被改写
- `StarSchema.cft(TOrderDesign.id, orderId).over();` → 正常转换

**断言：**

- 前两条原样保留（字面字符串仍可在 Service 文件中匹配到）。
- 第三条按 CftOnly 同样规则展开。
- 不应生成任何 `XxxWholeDTO` 之外的 DTO。

### 3. OoChainItTest

**目标：** 一对一维度表（`oo`）单个 / 多个串联两种分支。

**DSL（同 Service 两个方法）：**

- `assembleSingle(orderId)`：`cft(TOrderDesign.id, orderId).oo(TUserDesign.id).over();`
- `assembleMulti(orderId)`：`cft(TOrderDesign.id, orderId).oo(TUserDesign.id).oo(TAddressDesign.userId).over();`

**断言：**

- 生成两个 WholeDTO（或同一个被 RENAME 处理 —— 实际两次进入同一目标会触发 RENAME 流程，
  本用例为避免与用例 7 冲突，使用不同 cft：分别用 `TOrderDesign` 与 `TUser2Design`，或两方法
  同 cft 时检查 RENAME 文件落盘。**采用方案：两方法共用 `TOrderDesign` cft；预期产出
  `TOrderWholeDTO.java` + `TOrderWholeDTO1.java` 各一**）。
- WholeDTO 字段：单 oo → `tOrder + tUser`；多 oo → `tOrder + tUser + tAddress`，全部为单 Entity，
  无 List/Map/Multimap。
- Service 中两段链分别替换为：1-to-1 走 `.one()`、字段非 List。

> 备注：若希望 OoChain 与 RENAME 解耦，可改成两个方法分别使用不同 cft 表
> （例如 `TOrderDesign` 与 `TInvoiceDesign`），各产出独立 WholeDTO。

### 4. OmChainItTest

**目标：** 一对多维度表（`om`）单个 / 多个串联两种分支；验证复数命名 `English.plural` 与
`.list()` 选择。

**DSL（同 Service 两个方法）：**

- `assembleSingle`：`cft(TOrderDesign.id, id).om(TOrderItemDesign.orderId).over();`
- `assembleMulti`：`cft(TOrderDesign.id, id).om(TOrderItemDesign.orderId).om(TPaymentDesign.orderId).over();`

**断言：**

- WholeDTO 字段类型：`List<TOrderItemEntity> tOrderItems`、`List<TPaymentEntity> tPayments`。
- Service 替换：`java.util.List<TOrderItemEntity> tOrderItems = TOrderItemDesign.query()...list();`、
  `whole.setTOrderItems(tOrderItems);`。
- 验证两个 phrase 间 keys/mkeys 互不串台（两侧均空时仍正确清空）。

### 5. OmWithKeysAndMkeysItTest

**目标：** 一次覆盖 `key` / `mkey` 全部分支 —— 单 key、多 key、单 mkey、key+mkey 混合。

**DSL（同 Service 四个方法各一条链）：**

- `singleKey`：`.om(TOrderItemDesign.orderId).key(TOrderItemDesign.skuId).over();`
- `multipleKeys`：`.om(...).key(skuId).key(productId).over();`
- `singleMkey`：`.om(...).mkey(TOrderItemDesign.skuId).over();`
- `keyMkeyMixed`：`.om(...).key(skuId).mkey(productId).over();`

**断言（按方法分块）：**

- WholeDTO（仅落盘一次，多方法共用 cft → 出现 `TOrderWholeDTO` + RENAME 副本，或四方法用四张
  不同 cft 表彼此独立 —— **本用例采用四张事实表（`TOrder1` / `TOrder2` / `TOrder3` / `TOrder4`）
  避免 RENAME 干扰**）。
- key 路径：字段类型 `Map<Long, TOrderItemEntity> tOrderItemsEachSkuId`，Service 中
  `LinkedHashMap<Long, TOrderItemEntity> ... = new LinkedHashMap<>();` + foreach put。
- mkey 路径：字段类型 `com.google.common.collect.Multimap<Long, TOrderItemEntity>`，
  Service 中 `LinkedListMultimap<Long, TOrderItemEntity> ... = LinkedListMultimap.create();` + foreach put。
- 多 key / mkey 混合：foreach 块内多次 put（验证同一 phrase 的 keys/mkeys 在同一 foreach 内集中输出）。
- 验证 entity 字段类型解析（`entityFieldTypesEachFieldName`）正确（key 解析为 `Long` / `String` 等真实类型）。

### 6. MixedComprehensiveItTest

**目标：** 综合验证 oo + om + key + mkey 的字段顺序，跨 phrase 的 keys/mkeys 清空逻辑，
以及 **同方法内多条独立星型链**（`MultipleStarChains`）。

**DSL（同方法两条链）：**

```java
StarSchema.cft(TOrderDesign.id, orderId)
        .

oo(TUserDesign.id)
        .

om(TOrderItemDesign.orderId).

key(TOrderItemDesign.skuId).

mkey(TOrderItemDesign.tagId)
        .

om(TPaymentDesign.orderId)        // 此 om 不带 key/mkey，验证上一 om 收集的 key/mkey 已清空
        .

over();

StarSchema.

cft(TUserDesign.id, userId)
        .

om(TUserAddressDesign.userId)
        .

over();
```

**断言：**

- 生成 `TOrderWholeDTO` 与 `TUserWholeDTO` 两个文件。
- `TOrderWholeDTO` 字段顺序严格匹配：
  `tOrder` → `tUser` → `tOrderItems` → `tOrderItemsEachSkuId(Map)` → `tOrderItemsEachTagId(Multimap)` → `tPayments`。
- `TPayments` phrase **不应** 出现 `EachSkuId` / `EachTagId` 字段（验证 keys/mkeys 在 `om` 进入时被
  `keys.clear() / mkeys.clear()`）。
- Service 中两段原链均消失，分别展开为各自的查询/装配语句序列。
- 导入语句一次性 extract（无重复）。

### 7. WholeDTONamingAndRenameItTest

**目标：**

- WholeDTO 命名分支：cft Entity **不以** `Entity` 结尾时走 `entityName + postfix` 分支
  （配置 `isEntityEndWithEntity: false`，使生成的实体类名形如 `TOrder` 而非 `TOrderEntity`）。
- WholeDTO 重名：`FileExistenceResolutionEnum.RENAME` —— 预置同名文件，新生成文件以重命名后缀落盘。

**DSL：** 单方法单链：`StarSchema.cft(TOrderDesign.id, orderId).over();`

**fixture 预置：** 在 `src/main/java-st/com/example/dto/whole/TOrderWholeDTO.java`（与目标包一致）
预置一份 dummy `TOrderWholeDTO.java`，内容为人工占位 class（带特殊标记字段如
`String __preExisting`），随 java-st → java 拷贝步骤一起进入工作目录。

**断言：**

- 由于配置 `isEntityEndWithEntity: false`，Entity 类名为 `TOrder`，WholeDTO 名为 `TOrderWholeDTO`。
- 预置文件 `TOrderWholeDTO.java` 仍存在且包含 `__preExisting` 标记（未被覆盖）。
- 同包下出现重命名后的新文件（如 `TOrderWholeDTO1.java`），含本次生成的 `tOrder` 字段。
- Service 中 `whole` 变量类型应指向新生成的类（`TOrderWholeDTO1`）。

---

## 覆盖矩阵

| 用例                               | cft | oo | om | key | mkey | 多 phrase | 多 chain | RENAME |   命名分支    | 负向 |
|----------------------------------|:---:|:--:|:--:|:---:|:----:|:--------:|:-------:|:------:|:---------:|:--:|
| 1. CftOnlyItTest                 |  ✓  |    |    |     |      |          |         |        | 带 Entity  |    |
| 2. NotStarChainNoTransformItTest |  ✓  |    |    |     |      |          |         |        |           | ✓  |
| 3. OoChainItTest                 |  ✓  | ✓✓ |    |     |      |    ✓     |    ✓    |        |           |    |
| 4. OmChainItTest                 |  ✓  |    | ✓✓ |     |      |    ✓     |    ✓    |        |           |    |
| 5. OmWithKeysAndMkeysItTest      |  ✓  |    | ✓  | ✓✓  |  ✓✓  |          |    ✓    |        |           |    |
| 6. MixedComprehensiveItTest      |  ✓  | ✓  | ✓✓ |  ✓  |  ✓   |    ✓     |    ✓    |        |           |    |
| 7. WholeDTONamingAndRenameItTest |  ✓  |    |    |     |      |          |         |   ✓    | 不带 Entity |    |

合计 **7 个 IT 用例**，覆盖：

- `StarChainServiceImpl.detectStarChains` 的 StarSchema 根过滤分支与异常返回分支（用例 2）；
- `ChainMethodEnum` 全部 5 个分支：cft / oo / om / key / mkey（用例 1、3、4、5、6）；
- `analyzeRecursively` 跨 phrase `keys.clear() / mkeys.clear()`（用例 4、6）；
- `StarChainTransformerServiceImpl` 中 1-to-1 vs 1-to-many 分叉、Map vs Multimap 声明分叉、foreach 块生成、
  setter 名生成（用例 3 ~ 6）；
- `WholeDTOServiceImpl` 字段拼装顺序、Map/Multimap 类型生成、`FileExistenceResolutionEnum.RENAME` 冲突处理（用例 6、7）；
- `buildWholeDTOName` 两条命名分支：以 Entity 结尾 vs 不以 Entity 结尾（用例 1 vs 7）；
- 主循环 `StarTransformer.process()` 的多链同 CU、跨 CU 写回（用例 3 ~ 6）。

---

## 测试资源约定

每个用例在
`allison1875-cli/src/test/resources/it/star-transformer/<case>/`
下需提供：

```
<case>/
├── .allison1875.yml         # ddl + domain 配置；*Module 字段相对路径，由基类改写为绝对路径
├── pom.xml                  # 仅用于 MavenProjectClassLoaderUtils 构造 classloader
└── src/main/java-st/
    └── com/example/service/...Service.java   # 含 StarSchema 链的 Service 源码
    └── com/example/dto/whole/...WholeDTO.java # （仅用例 7）预置同名 WholeDTO
```

`.allison1875.yml` 关键字段：

- `domains[].persistenceModule / dtoModule / serviceImplModule` 等均设为 `"."`（单模块）
- `wholeDTOPackage: com.example.dto.whole`
- `entityPackage / designPackage / mapperPackage / mapperXmlDirs` 同 query-transformer
- `ddl` 至少包含 cft 表；用到 oo/om 时按需追加维度表 DDL
- `enableGenerateDesign: true` —— 必须，否则 `XxxDesign` / `PropertyName` 字段不会生成
- `isEntityEndWithEntity: true`（默认）—— 用例 1 ~ 6 使用；用例 7 关闭以验证另一命名分支

DSL 源码骨架（用例 5 OmWithKeysAndMkeys）：

```java
package com.example.service;

import com.example.design.TOrder1Design;
import com.example.design.TOrderItemDesign;
import com.spldeolin.allison1875.support.StarSchema;

public class OrderAssembleService {

    public void singleKey(Long orderId) {
        StarSchema.cft(TOrder1Design.id, orderId).om(TOrderItemDesign.orderId).key(TOrderItemDesign.skuId).over();
    }
    // multipleKeys / singleMkey / keyMkeyMixed 略
}
```

---

## 与 query-transformer 的差异（重要）

- query-transformer 关心 **SQL 生成**（Mapper.xml / Mapper.java）。
- star-transformer **不直接生成 SQL**，而是把星型链展开为对 `XxxDesign.query().byForced().fk.eq(...).one() / .list()`
  的多次调用 —— 真正的 SQL 由后续的 query-transformer pass 二次处理（在 form-generator
  组合工具中即如此串联）。因此本套 IT 仅断言 **Java 源码层面的展开结果与 WholeDTO 文件**，
  不去断言 Mapper.xml。
- 验证"展开后能再被 query-transformer 转换成 Mapper 调用"是 form-generator 集成测试的职责，
  不在本套 IT 范围内。

---

## 后续实现步骤（落地清单）

1. **新建** `StarTransformerItBaseTest`，以 `QueryTransformerItBaseTest` 为蓝本，把
   "拷贝 java-qt → 跑 query-transformer" 改为 "拷贝 java-st → 跑 star-transformer"。
   工作目录前缀使用 `target/it/st-<caseName>/`。
2. 为上表 7 个用例分别创建 `resources/it/star-transformer/<case>/` 资源目录与
   `*ItTest` 类。
3. 在 `allison1875-cli/pom.xml` 中无需新增依赖（star-transformer 已通过 cli 间接引入）。
4. `mvn install -DskipTests` 后 `mvn test -pl allison1875-cli -am -Dtest='*ItTest'`
   单跑本套 IT，确认全部通过；jacoco 报告应将
   `star-transformer/src/main/java/...` 的行覆盖率提升至接近 100%。
