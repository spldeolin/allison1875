## Gap 1: FilterPattern 衍生规则与前端参数映射

**事实**：后端 list 入参 DTO 中，所有可查询字段直接用原字段名，无"Like" / "List" / "Ge" 等后缀。例：
- `idCard: String` (like 过滤)
- `age: List<Long>` (in 过滤)
- `examDateStart / examDateEnd` (dateRange 过滤)

**前端影响**：plan C3 `request-builder.ts` 中的 `filterPatternListRules` 需要改造，改为直接用原字段名（text/like 和 in 时）或 `${name}Start/End`（dateRange/dateTimeRange 时）。

**用户决议**：改前端。

**实施**：✅ 更新 plan C3 request-builder.ts，使 filterPatternListRules 的字段名映射为：
- `like: (item, v) => v ? { [item.name]: v } : {}`
- `in: (item, v) => Array.isArray(v) && v.length ? { [item.name]: v } : {}`
- `ge/gt/le/lt: (item, v) => v != null ? { [item.name]: v } : {}`（与后端一致，无后缀）
- `dateRange/dateTimeRange: (item, v) => buildDateRange(item, v, 'Start', 'End')`（改为 Start/End，与后端一致）

---

## Gap 2: MultiSelect 在 List 出参的序列化形式

**事实**：补读后确认：
- list 出参：`ListStudentDormitoriesResp.facilities: List<FacilitiesEnum>` ✅
- detail 出参：`GetStudentDormitoryDetailResp.facilities: List<FacilitiesEnum>` ✅

**实施**：✅ protocol/response-parser.ts 中 multiSelect 的 parse 规则改为直接透传（无需 split）。

---

## Gap 3: Secret 字段在 List vs Detail 的可见性与脱敏

**事实**：
- secret 字段（doorPassword、emergencyContact）完全不出现在 list/detail 响应 DTO 中（已确认）
- save 入参时，initPattern/editPattern 决定是否允许用户指定

**用户决议**：
- secret 字段不返回，最多允许在新增、编辑表单时指定（取决于 initPattern 和 editPattern）

**实施**：✅ 
- SearchForm：secret 字段因为无 FilterPattern，自动隐藏（不参与查询）
- DataTable：secret 字段不出现在后端响应中，自动不显示
- EditModal：
  - create 模式：只有 doorPassword (initPattern=userInput) 可显示可编辑；emergencyContact (initPattern=todo) 隐藏
  - edit 模式：doorPassword (editPattern=userInput) 可显示可编辑；emergencyContact (editPattern=doNot) 隐藏
- Save 请求：doorPassword 可传值，emergencyContact 不传（因为 editPattern=doNot）

---

## Gap 4: InitPattern=todo 字段在 Save 入参的必填性

**用户决议**：
- initPattern=todo 表示"create 时用户不用提供，后端会在 controller/service 代码中由开发者手动初始化"
- emergencyContact：initPattern=todo（create 时隐藏），editPattern=doNot（edit 时隐藏）
- 结合两者，save 入参 DTO 中**不需要 emergencyContact 字段**

**实施**：✅ 
- plan C5 field-policy.ts：
  - `isVisible(item, 'edit-create')` → 检查 `item.initPattern !== 'doNot'`（todo 和 userInput 都显示）
  - `isReadonly(item, 'edit-create')` → 检查 `item.initPattern === 'todo'`（todo 字段 readonly，或在 isVisible 中改为隐藏）
  - 前端可选：todo 字段完全隐藏 vs todo 字段 readonly
- plan C3 buildSaveRequest：todo 字段也传值给后端，后端自行处理（或后端 DTO 中不包含该字段）

**建议**：前端隐藏 todo 字段（更简洁），避免用户误操作。

---

## Gap 5: FormDef Getter 污染 app.json

**事实**：✅ 验证完成
- app.json 中不含 `varName` / `bizIdName` / `entityName` 等 getter
- FormDef 已有适当 `@JsonIgnore` 注解
- **无需改动**

---

## 已确认无需处理的项

1. **Gap 2 - MultiSelect 格式**：✅ 确认为 `List<Enum>`，protocol/response-parser 直接透传
2. **Gap 5 - FormDef 污染**：✅ 无污染，不需要改 form-generator 或 app-generator
3. **Gap 6 - Delete 批量**：Delete 支持 List，plan C6 改为 `{ ${formName}Codes: [code] }`
4. **Gap 7 - 分页字段名**：改为 `pageNum` / `pageSize`（直接在 request-builder.ts hardcode）

---

## 用户决议总结

| Gap | 决议 | 实施 |
|---|---|---|
| 1. FilterPattern 字段名 | 改前端 | ✅ C3 request-builder.ts 改无后缀规则 |
| 2. MultiSelect 格式 | List<Enum> | ✅ C4 response-parser 直接透传 |
| 3. Secret 可见性 | 不返回，表单可指定 | ✅ SearchForm 隐藏，EditModal 按 init/edit pattern |
| 4. Todo 字段 | 前端隐藏，不传值 | ✅ C5 isVisible 返回 false；C3 buildSaveRequest 不包含 |
| 5. FormDef 污染 | 无污染 | ✅ 无需改 java |
| 6. Delete 批量 | 改前端支持 | ✅ C6 改为 List 格式 |
| 7. 分页字段 | pageNum/pageSize | ✅ C3 hardcode |

---

## 后续实施

所有决议已闭环。即可进入 **Phase C —— 改前端**。

关键改动：
- **C1**（types.ts）：新增 FilterPattern 类型，ItemDefBase **不加** filterPatterns 字段
- **C2**（endpoints.ts）：URL 推导规则已确认
- **C3**（request-builder.ts）：FilterPattern 规则改无后缀；分页字段改 pageNum/pageSize；buildSaveRequest 处理 todo 字段（隐藏）；delete 改 List
- **C4**（response-parser.ts）：multiSelect 直接透传
- **C5**（field-policy.ts）：FILTER_PATTERNS_BY_TYPE 常量表；isVisible 按 initPattern/editPattern 判断；isReadonly 判断 todo
- **C6**（CrudPage.vue）：delete 改 List；getDetail 按合并后的 bizKey 逻辑
- **C7**（SearchForm/EditModal/DataTable）：用 field-policy 的 isVisible / isReadonly
- **C8**（app.json）：示例 schema 跟随 types.ts 更新（无需 filterPatterns）

