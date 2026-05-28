# Backend Contract Gaps & Decisions

**Source**: contract.md derived from real generated code  
**Status**: Awaiting user decisions on 4 critical gaps

---

## Gap 1: FilterPattern 衍生规则与前端参数映射

**事实**：后端 list 入参 DTO 中，所有可查询字段直接用原字段名，无"Like" / "List" / "Ge" 等后缀。例：
- `idCard: String` (like 过滤)
- `age: List<Long>` (in 过滤)
- `examDateStart / examDateEnd` (dateRange 过滤)
- secret 字段不出现

**前端影响**：
- plan 中 C3 `request-builder.ts` 假设后端字段名规则为 `${name}Like` / `${name}List` 等，与实际不符
- 需要改造 `filterPatternListRules` 中的字段名生成规则，改为直接用原字段名（text/like 和 in 时）或 `${name}Start/End`（dateRange/dateTimeRange 时）

**建议处置**：
- **选项 A**：改 plan C3，使 `filterPatternListRules` 严格按实际后端规则（无后缀 for text/number/select/multiSelect，Start/End for range）
- **选项 B**：后端重新配置字段名后缀（工作量大，不推荐）

**推荐**：A（前端规则表改为无后缀规则，对标实际后端）

**待用户拍板**：☐

---

## Gap 2: MultiSelect 在 List 出参的序列化形式

**事实**：
- save 入参时：`facilities: List<FacilitiesEnum>`
- list 入参时：`facilities: List<FacilitiesEnum>`
- **list 出参时**：`ListStudentDormitoriesResp.facilities` 字段类型未读，**待观察是 `List<FacilitiesEnum>` 还是逗号分隔的 `String`**

**前端影响**：
- protocol/response-parser.ts 中 multiSelect 的 parse 规则（当前 plan 假设可能是 comma-separated string，需要 split）

**建议处置**：
- 等 gap 1 敲定后，读一次 `ListStudentDormitoriesResp.java` 确认 facilities 字段的实际类型
- 若是 List，response-parser 直接透传
- 若是 String，response-parser 需 split(",")

**推荐**：读代码确认，根据实际选择 parse 规则

**待用户拍板**：☐

---

## Gap 3: Secret 字段在 List vs Detail 的可见性与脱敏

**事实**：
- StudentDormitory 有两个 secret 字段：
  - `doorPassword` (initPattern=userInput, editPattern=userInput)
  - `emergencyContact` (initPattern=todo, editPattern=doNot)
- 当前代码中未读 List/Detail 出参 DTO，所以**不知道后端是否返回、返回时是否脱敏**

**前端影响**：
- SearchForm：secret 字段是否显示搜索项？（design 说不显示，取决于后端是否在 list 入参 DTO 中暴露）→ 实际后端已确认不暴露，可隐藏
- DataTable：list 出参中 secret 字段是否显示？（需要后端确认是否返回）
- EditModal：detail 出参中 secret 字段是否返回明文？
- 新建/编辑时 secret 字段的必填性（与 initPattern/editPattern 的后端语义相关）

**建议处置**：
- **选项 A**：前端硬编码规则"所有 secret 字段在 list/table 中隐藏，在 detail/edit 中显示"（假设后端会返回）
- **选项 B**：改 form-generator，使 secret 字段在 list 出参 DTO 中明确标记为 `@JsonIgnore`，只在 detail/save 中返回
- **选项 C**：实际集成时发现再调整（快速迭代）

**推荐**：C（先按 A 硬编码，联调时若后端返回形式不符再改 gaps）

**待用户拍板**：☐

---

## Gap 4: InitPattern=todo 字段在 Save 入参的必填性

**事实**：
- `StudentDormitory.emergencyContact` 的 `initPattern=todo`
- **plan C3 `buildSaveRequest` 假设 todo 字段也要传值**，但后端的实际语义是什么？

**后端可能行为**：
- todo 表示"后端会自动生成，前端不应该传"（此时 create 时不应传该字段，save req DTO 中也不该有该字段）
- todo 表示"前端可以传但不必传，后端有默认值"

**前端影响**：
- EditModal 的 required 标记（todo 字段是否应标记为 readonly 或隐藏）
- buildSaveRequest 是否应把 todo 字段的值传给后端

**建议处置**：
- **选项 A**：todo 字段在 create 时完全隐藏，edit 时也隐藏（假设后端自动管理）
- **选项 B**：todo 字段在 create 时为 readonly（显示但不可编辑），edit 时隐藏
- **选项 C**：todo 字段在 create/edit 时都传值给后端，由后端决定是否忽略

**推荐**：A（最安全，todo 字段由后端全权管理，前端不干扰；可在 field-policy.isReadonly 中硬编码 "todo→readonly"，或在 isVisible 中返回 false）

**待用户拍板**：☐

---

## Gap 5: FormDef Getter 污染 app.json

**事实**：
- app-generator 用 Jackson 的 `new ObjectMapper().writeValueAsString(appDef)` 序列化整个 AppDef
- `FormDef` 有若干计算 getter：`getVarName()`, `getBizIdName()`, `getBizIdGetterName()`, `getBizIdSetterName()`, `getEntityName()`
- 这些 getter **可能被序列化进 app.json**，污染前端的 schema

**前端影响**：
- app.json 大小增加（多余字段）
- 前端代码可能误用这些字段

**建议处置**：
- **选项 A**：改 form-generator 的 `FormDef` 给这些 getter 加 `@JsonIgnore`
- **选项 B**：改 app-generator，用自定义 ObjectMapper 配置忽略这些字段
- **选项 C**：验证后发现这些字段其实没被序列化（FormDef 已有 `@JsonIgnore` 在关键方法上），不需要改

**推荐**：先做 C（验证），再根据结果决定是否需要 A/B

**待用户拍板**：☐

---

## Gap 6: Delete 接口的批量 vs 单条

**事实**：
- 实际后端 delete 入参 DTO 为 `DeleteStudentProfileReq { studentProfileCodes: List<String> }`（复数）
- 支持批量删除
- plan C6 中的 `CrudPage.handleDelete` 假设单条删除，send `{ studentProfileCode: row.studentProfileCode }`

**前端影响**：
- 需要改 CrudPage.handleDelete，把单个 bizKey 改为 List：`{ studentProfileCodes: [row.studentProfileCode] }`
- 或者前端暂时保留单条删除的交互，但请求体改为 list 格式

**建议处置**：
- **选项 A**：前端保留单条删除的用户交互，但把请求改为 `{ ${bizKey}Codes: [code] }`
- **选项 B**：前端实现批量删除（多选 checkbox），同时支持单条删除（也改为 list 格式）

**推荐**：A（快速对齐后端，保留现有交互）

**待用户拍板**：☐

---

## Gap 7: 分页字段名

**事实**：
- 实际后端 list 入参为 `pageNum` / `pageSize`（不是 plan 假设的 `pageNo` / `pageSize`）

**前端影响**：
- plan C6 CrudPage 中 `buildListRequest` 使用的分页字段名需要改
- endpoints.ts 可能需要 constant 定义分页字段名

**建议处置**：
- plan C3 `buildListRequest` 改为使用 `pageNum` / `pageSize`（硬编码或 constant）

**推荐**：在 request-builder.ts 中 hardcode `pageNum` / `pageSize`

**待用户拍板**：☐ (此项低风险，直接改)

---

## 已确认无需处理的项

1. **BaseUrl 推导**：后端 `@RequestMapping("/api/v1/studentProfile")` 清晰，规则为 `/api/v1/${lowerCamelFormName}`，plan C2 endpoints.ts 可直接实现
2. **Enum 编码格式**：所有 select/multiSelect 枚举在 JSON 中为 code 字符串，符合设计
3. **时间格式**：后端统一用 ISO 格式（`yyyy-MM-dd` / `HH:mm:ss` / `yyyy-MM-dd HH:mm:ss`），前端 Naive UI 组件能消费，无需转换
4. **RequestResult 结构**：后端骨架固定为 `{ errorCode, data, errorMsg, traceId }`，与 plan C6 中 CrudPage 使用的 `request.ts` 拦截器应该对齐（需验证）
5. **业务主键字段名**：`${lowerCamelFormName}Code` 的规则清晰，可在 CrudPage 中动态生成

---

## 用户决议模板

请对以下 5 个 Gap 逐一拍板：

**Gap 1 - FilterPattern 字段名规则**：☐ A（推荐）/ ☐ B / ☐ 其他
**Gap 2 - MultiSelect 序列化形式**：☐ 等读代码 / ☐ 其他
**Gap 3 - Secret 字段可见性**：☐ A / ☐ B / ☐ C（推荐）
**Gap 4 - InitPattern=todo 必填性**：☐ A（推荐）/ ☐ B / ☐ C
**Gap 5 - FormDef Getter 污染**：☐ C 先验证（推荐）/ ☐ A / ☐ B
**Gap 6 - Delete 批量支持**：☐ A（推荐）/ ☐ B
**Gap 7 - 分页字段名**：☐ 改为 pageNum/pageSize（推荐，直接做）

---

## 实施计划（待用户拍板后）

- Gap 1, 7：更新 plan C3 request-builder.ts
- Gap 2：读 ListStudentDormitoriesResp.java，补充确认
- Gap 3, 4：更新 plan C5 field-policy.ts 的 isVisible / isReadonly 规则
- Gap 5：运行 AppGenerator 或读 app.json 验证
- Gap 6：更新 plan C6 CrudPage.vue 的 handleDelete 方法

后续可在 Phase C 实施时逐个修复。
