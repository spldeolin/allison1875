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

### OnOffItemItTest

- 验证 onOff 类型字段的处理
- DDL 中列类型为 `TINYINT(1)`
- DTO 中字段类型为 `Boolean`
- List API 的 Req 中该字段为 `List<Boolean>` 列表过滤

---

## 索引生成

### UniqueIndexItTest

- 验证 `isUnique=true` 的索引在 DDL 中生成 `UNIQUE KEY`
- persistence-generator 基于唯一索引生成 `queryByXxx` 单条查询方法

### CompositeIndexItTest

- 验证多字段联合索引（`itemNames` 包含多个字段）的 DDL 生成
- 索引名为 `uk_col1_col2_col3`（唯一）或 `idx_col1_col2`（普通）
- persistence-generator 基于联合索引生成最左前缀查询方法

### IndexNameTruncationItTest

- 验证索引名超过 64 字符时被自动截断
- DSL 中使用多个长字段名组成索引

---

## 初始化/编辑模式（InitOrEditPattern）

### InitPatternTodoItTest

- 验证 `initPattern=todo` 的字段在 Save API 的 Service 中生成 `// TODO 请补充初始值` 注释
- 该字段不出现在 Save Req DTO 中（非用户输入）

### EditPatternDoNotItTest

- 验证 `editPattern=doNot` 的字段在编辑时不被更新
- Save API 的 Service 中，编辑分支不包含该字段的 setter 调用

### MixedInitEditPatternItTest

- 验证 `initPattern=userInput` + `editPattern=doNot` 组合
- 该字段仅在创建时由用户输入，编辑时不可修改
- Save API 的 Service 中，该字段 setter 仅出现在 `if (toCreate)` 分支内

---

## 多表单

### MultiFormItTest

- 验证 DSL 包含多个 FormDef 时，每个表单独立生成全套文件
- 各表单的 DDL、Entity、Mapper、Controller、Service、DTO、枚举互不干扰
- 每个 Controller 的 `@RequestMapping` 路径使用各自表单的 `varName`

---

## 审计字段与业务主键

### CommonItemsAutoAddItTest

- 验证 form-generator 自动添加的公共字段
- `xxxCode`（业务主键）：自动添加为 items 首位，类型 text，maxLength=36，initPattern=TODO，editPattern=DO_NOT
- `createdAt`（创建时间）：自动添加为 items 末尾，initPattern=TODO，editPattern=DO_NOT
- `updatedAt`（更新时间）：自动添加为 items 末尾，initPattern=TODO，editPattern=TODO
- 自动为业务主键创建唯一索引

---

## API 生成验证

### SaveApiItTest

- 验证 Save API 的完整生成
- Controller 中生成 `saveXxx` handler（POST），包含 Req（业务 ID + 用户输入字段）和 Resp（业务 ID）
- Service 中包含 `toCreate` 判断逻辑（业务 ID 为 null 则创建，否则编辑）
- 创建分支：new Entity → setBizId(shortUuid) → setCreatedAt
- 编辑分支：queryByBizId → null 检查 → 抛 RuntimeException

### ListApiItTest

- 验证 List API 的完整生成
- Controller 中生成 `listXxxs` handler（POST），Req 包含各字段过滤条件 + 分页参数（pageNum/pageSize）
- Resp 包含非 secret 的所有字段，附加 `@P` 注解（分页返回）
- Service 中包含 Design Chain 查询（按更新时间倒序 + 分页）
- 各类型字段在过滤条件中的运算符：number/onOff/select → `.in()`，text → `.like()`，time → `.ge()/.le()`

### GetDetailApiItTest

- 验证 GetDetail API 的完整生成
- Controller 中生成 `getXxxDetail` handler（POST），Req 包含业务 ID（`@NotNull`）
- Resp 包含非 secret 的所有字段
- Service 中包含按业务主键查询 + null 检查 + 字段转换（select → enum、time → localDate/localTime）

### DeleteApiItTest

- 验证 Delete API 的完整生成
- Controller 中生成 `deleteXxx` handler（POST），Req 包含业务 ID 列表（`@NotEmpty`）
- Service 中使用 Design Chain 的 `.delete().where().bizId.in(ids).over()` 模式
- 有 multiSelect 字段时，级联删除关联表

---

## doc-analyzer 集成

### EnableDocAnalyzerItTest

- 验证 `enableDocAnalyzer=true` 配置时，form-generator 在最后阶段调用 doc-analyzer
- 生成 API 文档（Markdown 或 DSL），文档内容涵盖 save/list/getDetail/delete 四个 API
- `mvcHandlerQualifierWildcards` 被自动设置为生成的 Controller 的 handler 范围

### DisableDocAnalyzerItTest

- 验证 `enableDocAnalyzer=false`（或未配置）时，不调用 doc-analyzer
- 不生成 api-docs / api-dsls 目录

---

## 空 DSL 与边界

### EmptyFormsItTest

- 验证 DSL 文件中表单列表为空时，工具正常结束并输出 warn（"no form definitions detected"）
- 不生成任何文件

---

## 不纳入集成测试的模块

以下功能路径在当前版本中标记为 TODO 或被注释，**不计划通过集成测试覆盖**：

- **排序字段枚举**（`SortItemEnum`）：代码中 `CompilationUnitUtils.writeJava(cu)` 被注释，注释说明「query-transformer 能力不支持，暂时固定为更新时间倒序」
- **动态排序参数**（`sortItem` + `isSortAsc`）：List API 中相关代码被注释
- **checkDuplicate 逻辑**：DSL 中 `TextItemDef.checkDuplicate` 和 `NumberItemDef.checkDuplicate` 字段存在定义但未见 Service 中消费
- **filterPatterns 逻辑**：DSL 中 `TextItemDef.filterPatterns` 字段存在定义但 ListApiServiceImpl 中统一使用 `.like()` 过滤
