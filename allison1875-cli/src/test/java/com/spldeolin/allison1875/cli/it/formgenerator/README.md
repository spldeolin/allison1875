# form-generator 集成测试用例说明

本目录包含 form-generator 工具的全部集成测试用例。每个测试通过基类 `FormGeneratorItBaseTest` 将测试资源（含
YAML DSL 和 `.allison1875.yml`）拷贝到临时目录、改写配置为绝对路径，然后调用 `Bootstrap.main()` 执行
form-generator，最后在生成的 DDL、Entity、Mapper、Mapper XML、Controller、Service、DTO、枚举文件上进行断言。

form-generator 的核心职责是：解析 YAML 表单 DSL（`FormDef` 列表），编排调用五阶段流水线——DDL 生成 →
persistence-generator（Entity/Mapper/XML/Design）→ 枚举生成 → handler-transformer（Controller/Service/DTO）→
query-transformer（Design Chain → Mapper 调用），自动生成完整的 CRUD 全栈代码。

---

## 基本流程

### BasicFormItTest ✅ 已完成

- 验证单表单最小 DSL 的完整端到端流程
- DSL 包含一个 text 字段和一个 number 字段
- 生成 DDL 文件（`sql/ddl.sql`），包含 CREATE TABLE 语句、主键列、业务主键列、审计字段列
- 生成 Entity 文件，包含自动添加的 `xxxCode`（业务主键）、`createdAt`、`updatedAt` 字段
- 生成 Mapper 接口和 Mapper XML（含 insert、queryById、索引查询等方法）
- 生成 Controller 文件，包含 save/list/getDetail/delete 四个 handler
- 生成 Service 接口和 ServiceImpl 文件（按 handler 分拆：SaveBookService、ListBooksService、GetBookDetailService、DeleteBookService）
- 生成 Req/Resp DTO 文件
- 业务主键索引（唯一索引）被正确创建
- 实现日期：2026-05-17

---

## 字段类型覆盖

### TextItemItTest ✅ 已完成

- 验证 text 类型字段的全路径处理
- DDL 中生成 `VARCHAR(n)` 列，n 取自 `maxLength`；多行文本生成 `LONGTEXT`
- SaveReq DTO 中字段类型为 `String`，`isNonVoid=true` 时附加 `@NotBlank`，所有 text 字段附加 `@Size(min=0, max=n)`
- ListReq 中该字段为模糊匹配过滤条件（单值 `String`），Design Chain 被 query-transformer 转换为 Mapper 调用
- GetDetailResp 中该字段正常返回
- 实现日期：2026-05-17

### NumberItemItTest ✅ 已完成

- 验证 number 类型字段的完整处理
- `canBeDecimal=false` → DDL 中 `BIGINT`，DTO 中 `Long`（`isNonVoid` 时附加 `@NotNull`）
- `canBeDecimal=true` → DDL 中 `DECIMAL(14, 4)`，DTO 中 `BigDecimal`
- `canBeDecimal=false` 的字段在 DTO 中附加 `@JsonSerialize(using = ToStringSerializer.class)`（Long → String 防前端精度丢失）
- ListReq 中为 `List<BigDecimal>` 或 `List<Long>` 列表过滤（`.in()`）
- 实现日期：2026-05-17

### TimeItemItTest ✅ 已完成

- 验证 time 类型字段在不同 `format` 下的处理
- `format=date` → DDL 中 `DATETIME`（存储统一为 LocalDateTime），DTO 中 `LocalDate`，附加 `@JsonFormat(pattern="yyyy-MM-dd")`
- `format=dateTime` → DTO 中 `LocalDateTime`，附加 `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")`
- `format=time` → DTO 中 `LocalTime`，附加 `@JsonFormat(pattern="HH:mm:ss")`
- ListReq 中生成 `xxxStart` 和 `xxxEnd` 两个范围过滤字段；query-transformer 将其映射为 Param 中的 `xxx`/`xxxEx`（统一 LocalDateTime 转换）
- Entity 中 time 字段始终为 `LocalDateTime`（存储层统一）；GetDetailResp / ListResp 通过 `.toLocalDate()` / `.toLocalTime()` 转换
- 修复了 2 处程序 BUG：`isNonVoid=false` + `format=time/date` 时 null 检查表达式错误使用类名 `EventTime` 而非 getter `event.getEventTime()`
- 实现日期：2026-05-17

### SelectItemItTest ✅ 已完成

- 验证 select 类型字段的枚举生成和引用
- 生成 `XxxEnum.java` 枚举文件，包含 `code`、`title` 字段和 `of()`、`valid()` 方法
- DTO 中字段类型引用生成的枚举类型
- DDL 中列类型为 `VARCHAR(64)`（存储枚举 code）
- Save API 的 Service 逻辑中包含 `.getCode()` 转换（枚举 → String）；`isNonVoid=false` 时包含 null 安全判断
- GetDetail API 的 Service 逻辑中包含 `XxxEnum.of()` 转换（String → 枚举）
- ListReq 中 select 字段生成 `List<EnumType>` 列表过滤
- 实现日期：2026-05-17

### MultiSelectItemItTest ✅ 已完成

- 验证 multiSelect 类型字段的关联表生成
- 生成额外的关联表（association form）DDL：`CREATE TABLE student_hobbies`，包含主表业务主键列（`student_code`）和选项 code 列（`hobbies`）
- 关联表生成独立的 Entity（`StudentHobbiesEntity`）、Mapper（`StudentHobbiesMapper`）、XML
- Save API 的 Service 逻辑中包含「先删后建」关联实体的 forEach 循环（`studentHobbiesMapper.deleteByStudentCode` → `req.getHobbies().forEach` → `studentHobbiesMapper.insert`）
- Delete API 的 Service 逻辑中包含关联表的级联删除（Design Chain 被 query-transformer 转为 `studentHobbiesMapper.deleteStudentHobbies` 调用）
- GetDetail API 的 Service 逻辑中包含关联表查询 + stream map 转枚举（`queryByStudentCode → stream → map(StudentHobbiesEntity::getHobbies) → map(HobbiesEnum::of) → collect`）
- DTO 中包含 `List<HobbiesEnum>` 类型的字段（SaveReq / GetDetailResp / ListReq），`@NotEmpty` 校验
- 修复了 1 处断言问题：Delete ServiceImpl 中 Design Chain 已被 query-transformer 转换为 Mapper 调用
- 实现日期：2026-05-17

### SecretItemItTest ✅ 已完成

- 验证 secret 类型字段的特殊处理
- DDL 中列类型为 `VARCHAR(255)`
- Save API 的 Req 中包含该字段（`String apiKeySecret` + `@NotEmpty`），供创建/编辑时设置
- Save ServiceImpl 中正常执行 `entity.setApiKeySecret(req.getApiKeySecret())`
- List API 的 Resp 中**不包含**该字段（secret 不在列表中展示）
- GetDetail API 的 Resp 中**不包含**该字段（secret 不返回）
- List API 的 Req 过滤条件中**不包含**该字段
- 实现日期：2026-05-17

### OnOffItemItTest ✅ 已完成

- 验证 onOff 类型字段的处理
- DDL 中列类型为 `TINYINT(1)`
- DTO 中字段类型为 `Boolean`（SaveReq / GetDetailResp / ListResp）
- `isNonVoid=true` 时 SaveReq 中附加 `@NotNull`
- List API 的 Req 中该字段为 `List<Boolean>` 列表过滤（与 NUMBER 共用 IN 过滤路径）
- 实现日期：2026-05-17

---

## 索引生成

### UniqueIndexItTest ✅ 已完成

- 验证 `isUnique=true` 的索引在 DDL 中生成 `UNIQUE KEY`
- persistence-generator 基于唯一索引生成 `queryByXxx` 单条查询方法（返回单个 Entity，非 `List<Entity>`）
- Mapper XML 中包含对应的 `<select id="queryByXxx">` 精确匹配语句
- Save ServiceImpl 编辑分支使用 `userMapper.queryByUserCode(...)` 查询已有记录
- 实现日期：2026-05-17

### CompositeIndexItTest ✅ 已完成

- 验证多字段联合唯一索引（`itemNames: [orderNo, customerId], isUnique: true`）的完整处理
- DDL 中生成 `UNIQUE KEY \`uk_order_no_customer_id\` (\`order_no\`, \`customer_id\`)`
- persistence-generator 将联合索引平铺为最左前缀：`queryByOrderNo`（非唯一 → `List<OrderEntity>`）和 `queryByOrderNoCustomerId`（唯一 → `OrderEntity`）
- Mapper XML 中包含对应的多列精确匹配 `<select>` 语句
- 实现日期：2026-05-17

### IndexNameTruncationItTest ✅ 已完成

- 验证索引名超过 64 字符时被自动截断（79 → 64 字符）
- DSL 中使用 3 个长字段名组成唯一索引，完整名 `uk_very_long_field_name_one_very_long_field_name_two_very_long_field_name_three` 被截断为 `uk_very_long_field_name_one_very_long_field_name_two_very_long_f`
- 原始完整索引名不出现在 DDL 中，截断后的 64 字符名以 `UNIQUE KEY` 出现
- Mapper 方法名（`queryByVeryLongFieldNameOneVeryLongFieldNameTwoVeryLongFieldNameThree` 等）不受截断影响
- 实现日期：2026-05-17

---

## 初始化/编辑模式（InitOrEditPattern）

### InitPatternTodoItTest ✅ 已完成

- 验证 `initPattern=todo` / `editPattern=doNot` 字段的生成行为
- 该字段不出现在 SaveReq DTO 中（非 `userInput`，不进入 Req）
- Save ServiceImpl 的 `if (toCreate)` 分支中生成 `task.setAssigneeId(""); // TODO 请补充初始值`
- TODO 注释位于 `setTaskCode` 之后、`setCreatedAt` 之前（创建分支内部）
- 编辑分支（else）中不包含该字段的 setter 调用（editPattern=doNot）
- 实现日期：2026-05-17

### EditPatternDoNotItTest ✅ 已完成

- 验证 `editPattern=doNot` 字段在编辑时不被更新
- 该字段仍出现在 SaveReq 中（initPattern=userInput，创建时仍可输入）
- 创建（toCreate）分支中正常设置该字段
- common 节中不设置（editPattern != userInput），else/edit 分支中不设置（editPattern=doNot）
- 同一表单中普通字段（title/content）仍在 common 节正常设置，不受影响
- 实现日期：2026-05-17

### MixedInitEditPatternItTest ✅ 已完成

- 验证 `initPattern=userInput` + `editPattern=doNot` 组合
- 该字段（skuCode）出现在 SaveReq 中，用户可在创建时输入
- skuCode 的 setter 仅出现在 `if (toCreate)` 分支内（validated via `firstSkuSet == lastSkuSet`）
- common 节和 else/edit 分支中均不设置该字段
- 同一表单中普通字段（productName/remark）在 common 节正常设置，互不干扰
- 实现日期：2026-05-17

---

## 多表单

### MultiFormItTest ✅ 已完成

- 验证 DSL 包含多个 FormDef（Book + Author）时，每个表单独立生成全套文件
- 两个表单（Book、Author）均独立生成 DDL、Entity、Mapper、Mapper XML、Controller、Service（4 个 API）、DTO、Design
- DDL 中包含两个 `<code>CREATE TABLE</code>` 语句，分别对应 `book` 和 `author` 表
- Book 的实体/Service/DTO 不包含 Author 表单的字段（authorName、nationality），反之亦然
- BookController 的 `@RequestMapping` 路径为 `/api/v1/book`，AuthorController 为 `/api/v1/author`
- Author 中的 select 字段生成了 `NationalityEnum` 枚举，Book 无枚举生成
- 修复了 1 处断言过严问题：SaveAuthorServiceImpl 不直接包含 `NationalityEnum` 字符串（通过通配符 import），改为断言 `.getCode()` 调用
- 实现日期：2026-05-17

---

## 审计字段与业务主键

### CommonItemsAutoAddItTest ✅ 已完成

- 验证 form-generator 自动添加的公共字段（DSL 中只定义 productName + stock，三个公共字段由工具自动注入）
- `productCode`（业务主键）：自动添加为 items 首位，类型 text，maxLength=36，DDL 中 `VARCHAR(36) NOT NULL`，自动创建唯一索引 `uk_product_code`
- `createdAt`（创建时间）：自动添加为 items 末尾，DDL 中 `DATETIME NOT NULL`，Entity 中有 `createdAt` 字段
- `updatedAt`（更新时间）：自动添加为 items 末尾，DDL 中 `DATETIME NOT NULL`，Entity 中有 `updatedAt` 字段
- SaveReq 中 productCode 作为业务 ID 存在（编辑时查已有记录用），但无 `@NotBlank`/`@NotNull` 校验注解，createdAt/updatedAt 不出现在 SaveReq 中
- Save ServiceImpl 的创建分支使用 `UUID.randomUUID()`（shortUuid）生成 productCode，非 TODO 注释模式
- `createdAt` 只在创建分支设置（editPattern=DO_NOT），`updatedAt` 在 common 节（创建+编辑均设置）
- 修复了 2 处断言问题：productCode 实际出现在 SaveReq 中（作为编辑时的 bizId），且 productCode 由 shortUuid 自动生成而非 TODO 注释
- 实现日期：2026-05-17

---

## API 生成验证

### SaveApiItTest ✅ 已完成

- 验证 Save API 的完整生成，DSL 包含 text、number（int+decimal）、select、onOff 多种字段类型
- Controller 中生成 `saveItem` handler（POST），入参为 SaveItemReq（含 itemCode bizId + 用户字段），出参为 SaveItemResp（itemCode）
- SaveItemReq 中各字段校验注解正确：`@NotBlank`（nonVoid text）、`@NotNull`（nonVoid number/onOff）
- Service 中有 `boolean toCreate = req.getItemCode() == null` 创建/编辑判断
- 创建分支：`new ItemEntity()` → `setItemCode(UUID.randomUUID())` → `setCreatedAt(LocalDateTime.now())` → 各字段 setter → `itemMapper.insert(item)`
- 编辑分支：`itemMapper.queryByItemCode(req.getItemCode())` → null 检查 → `throw new RuntimeException(...)` → 各字段 setter → `itemMapper.updateById(item)`
- select 字段（category）通过 `req.getCategory().getCode()` 转换，onOff/number 直接 set
- `updatedAt` 在 common 节（if/else 之后）统一设置为当前时间
- 返回 `new SaveItemResp().setItemCode(item.getItemCode())`
- 实现日期：2026-05-17

### ListApiItTest ✅ 已完成

- 验证 List API 的完整生成，DSL 包含 text、number（int+decimal）、select、onOff 多种字段类型
- Controller 中生成 `listItems` handler（POST），返回 `PageResult<ListItemsResp>`
- ListReq 中各字段过滤条件类型正确：text → `String`（LIKE），number(int) → `List<Long>`（IN），number(decimal) → `List<BigDecimal>`（IN），select → `List<CategoryEnum>`（IN），onOff → `List<Boolean>`（IN）
- ListReq 中 itemCode 作为 bizId 自动添加为 `List<String>` IN 过滤器
- ListReq 中包含分页参数 `Integer pageNum = 1` / `Integer pageSize = 10`
- ListReq 中 createdAt 自动添加为 createdAtStart/createdAtEnd 范围过滤（`LocalDateTime`）
- ListResp 包含所有非 secret 字段（itemCode/text/number/isActive/remark/createdAt/updatedAt）
- ServiceImpl 中 Design Chain 已被 query-transformer 成功转换为 `itemMapper.countItem` + `itemMapper.queryItem` 调用
- Param DTO（QueryItemParam）被正确构建，分页通过 `setOffset`/`setLimit` 传递，枚举 stream map `.getCode()` 转换
- 空结果返回 `new PageResult<>()`，非空返回 `new PageResult<>(total, dtos)`
- 修复了 3 处断言问题：itemCode 实际出现在 ListReq 中（bizId IN 过滤）、@P 注解未生成（改用 PageResult<ListItemsResp> 验证）、pageNum/pageSize 大小写不匹配
- 实现日期：2026-05-17

### GetDetailApiItTest ✅ 已完成

- 验证 GetDetail API 的完整生成，DSL 包含 text、number（int+decimal）、select、onOff 多种字段类型
- Controller 中生成 `getItemDetail` handler（POST），入参 GetItemDetailReq（itemCode + @NotNull），出参 GetItemDetailResp
- GetItemDetailReq 中 itemCode 附有 `@NotNull` 校验注解
- GetItemDetailResp 包含所有非 secret 字段：itemCode、itemName、quantity、unitPrice、CategoryEnum category、isActive、remark、createdAt、updatedAt
- ServiceImpl 按业务主键查询 `itemMapper.queryByItemCode(req.getItemCode())` → null 检查 → `throw new RuntimeException(...)`
- select 字段通过 `CategoryEnum.of(item.getCategory())` 转换 String → 枚举
- 各字段逐一 set 到 Result DTO，最后 `return result`
- 测试一次通过，无需修复
- 实现日期：2026-05-17

### DeleteApiItTest ✅ 已完成

- 验证 Delete API 的完整生成，DSL 包含 text、onOff、multiSelect 多种字段类型（含 tags 多选字段用于验证级联删除）
- Controller 中生成 `deleteTask` handler（POST），返回 void，引用 DeleteTaskReq
- DeleteTaskReq 包含 `List<String> taskCode` 业务 ID 列表 + `@NotEmpty` 校验注解
- ServiceImpl 中 Design Chain 已被 query-transformer 成功转换为 Mapper 调用：
  - 关联表级联删除：`taskTagsMapper.deleteTaskTags(...)`（先删关联表）
  - 主表删除：`taskMapper.deleteTask(...)`
- 关联表 `TaskTagsMapper` 和主表 `TaskMapper` 均生成独立的 Mapper 接口和 XML
- 测试一次通过，无需修复
- 实现日期：2026-05-17

---

## doc-analyzer 集成

### EnableDocAnalyzerItTest ✅ 已完成

- 验证 `enableDocAnalyzer=true` + `flushTo: [MARKDOWN]` + `markdownDir: api-docs` 配置时，form-generator 在最后阶段调用 doc-analyzer
- api-docs 目录和 Markdown 文件被正确生成（日志显示 `create markdown file. file=.../api-docs/笔记.md`）
- 文档包含 4 个 endpoint（日志显示 `endpoints.size=4`），涵盖 saveNote/listNotes/getNoteDetail/deleteNote 四个 API
- 文档内容包含 HTTP 方法（POST）、请求路径（`/api/v1/note`）、Markdown heading
- form-generator 主体流程不受影响：DDL/Entity/Controller 等代码文件仍然正常生成
- 修改了 FormGeneratorItBaseTest 基类，增加了 `markdownDir` 路径解析支持
- 测试一次通过，无需修复
- 实现日期：2026-05-17

### DisableDocAnalyzerItTest ✅ 已完成

- 验证 `enableDocAnalyzer=false` 显式配置时，不调用 doc-analyzer
- api-docs 目录不被生成，但 DDL/Entity/Controller 等代码文件仍然正常生成
- 测试一次通过
- 实现日期：2026-05-17

---

## 空 DSL 与边界

### EmptyFormsItTest ✅ 已完成

- 验证 DSL 文件中表单列表为空（`[]`）时，工具正常结束不生成文件
- DDL 文件不被生成
- Entity/Controller 目录不生成任何实体文件
- api-docs 目录不被生成
- 测试一次通过
- 实现日期：2026-05-17

---

## 不纳入集成测试的模块

以下功能路径在当前版本中标记为 TODO 或被注释，**不计划通过集成测试覆盖**：

- **排序字段枚举**（`SortItemEnum`）：代码中 `CompilationUnitUtils.writeJava(cu)` 被注释，注释说明「query-transformer 能力不支持，暂时固定为更新时间倒序」
- **动态排序参数**（`sortItem` + `isSortAsc`）：List API 中相关代码被注释
- **checkDuplicate 逻辑**：DSL 中 `TextItemDef.checkDuplicate` 和 `NumberItemDef.checkDuplicate` 字段存在定义但未见 Service 中消费
- **filterPatterns 逻辑**：DSL 中 `TextItemDef.filterPatterns` 字段存在定义但 ListApiServiceImpl 中统一使用 `.like()` 过滤
