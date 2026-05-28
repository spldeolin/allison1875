# Backend Contract — Derived From Generated Code

**Source**: form-generator 输出于 `/Users/Deolin/Documents/project-repo/github/allison1875/output/super-dsl-example/super-dsl-example-backend`  
**DSL**: super-dsl.yml (app DSL format, 生成的是完整后端 = backend-skeleton + form-generator CRUD 代码)

---

## 1. URL 表

| Form | Base Path | save | list | getDetail | delete |
|---|---|---|---|---|---|
| StudentProfile | `/api/v1/studentProfile` | `POST /api/v1/studentProfile/saveStudentProfile` | `POST /api/v1/studentProfile/listStudentProfiles` | `POST /api/v1/studentProfile/getStudentProfileDetail` | `POST /api/v1/studentProfile/deleteStudentProfile` |
| StudentDormitory | `/api/v1/studentDormitory` | `POST /api/v1/studentDormitory/saveStudentDormitory` | `POST /api/v1/studentDormitory/listStudentDormitories` | `POST /api/v1/studentDormitory/getStudentDormitoryDetail` | `POST /api/v1/studentDormitory/deleteStudentDormitory` |
| StudentExam | `/api/v1/studentExam` | `POST /api/v1/studentExam/saveStudentExam` | `POST /api/v1/studentExam/listStudentExams` | `POST /api/v1/studentExam/getStudentExamDetail` | `POST /api/v1/studentExam/deleteStudentExam` |

## 2. URL 推导规则

- **base path** = `/api/v1/${lowerCamelFormName}`（例：`StudentProfile` → `studentProfile`）
- **save** = `base + /save${FormName}`（例：`/api/v1/studentProfile/saveStudentProfile`）
- **list** = `base + /list${FormName}s`（例：`/api/v1/studentProfile/listStudentProfiles`）
- **getDetail** = `base + /get${FormName}Detail`（例：`/api/v1/studentProfile/getStudentProfileDetail`）
- **delete** = `base + /delete${FormName}`（例：`/api/v1/studentProfile/deleteStudentProfile`）

均为 POST 方法。

---

## 3. 通用 Response 格式

```json
{
  "errorCode": null,         // 成功时为 null；失败时为错误码
  "data": { ... },           // 实际数据；失败时为 null
  "errorMsg": null,          // 失败时的错误信息
  "traceId": "..."           // 请求追踪 ID
}
```

**后端骨架固定**，form-generator 生成的 controller 均返回 `RequestResult<T>`。

---

## 4. List 接口

### 4.1 入参 DTO 规则

**分页字段**：`pageNum` (默认 1) / `pageSize` (默认 10)

**FilterPattern 衍生规则**（基于观察的三个 form）：

| ItemType | FilterPattern | DTO 字段名规则 | DTO 字段类型 | 例 |
|---|---|---|---|---|
| text | like | 无后缀 | String | idCard / studentName / bio |
| text/number/select/multiSelect/onOff | in | **无后缀**，但字段值为 List | List<T> | `age: List<Long>` / `gender: List<GenderEnum>` |
| time | dateRange / dateTimeRange | `${name}Start` + `${name}End` | LocalDate/LocalTime/LocalDateTime | examDate → examDateStart / examDateEnd |
| secret | — | 不出现在 DTO | — | — |
| 审计字段 | — | `createdAtStart` / `createdAtEnd` | LocalDateTime | 自动注入 |

**关键观察**：
- 后端不区分"like"和"in"的字段命名；所有可查询字段直接用原字段名，无"Like" / "List"后缀
- IN 过滤总是用 List 类型的字段（例 `studentProfileCode: List<String>`）
- 日期范围总是拆成 `${name}Start` + `${name}End`
- secret 字段（无 FilterPattern）完全不在 list 入参 DTO 中
- multiSelect 字段在入参里也是 List（例 `facilities: List<FacilitiesEnum>`）

### 4.2 每个 Form 的 List 入参 DTO

#### StudentProfile
```
- studentProfileCode: List<String>        [in]
- idCard: String                          [like]
- studentName: String                     [like]
- bio: String                             [like]
- age: List<Long>                         [in]
- gender: List<GenderEnum>                [in]
- createdAtStart: LocalDateTime
- createdAtEnd: LocalDateTime
- pageNum: Integer = 1
- pageSize: Integer = 10
```

#### StudentDormitory
```
- studentDormitoryCode: List<String>      [in]
- studentId: List<Long>                   [in]
- hasAllergy: List<Boolean>               [in]
- facilities: List<FacilitiesEnum>        [in]
- monthlyRent: List<BigDecimal>           [in]
- createdAtStart: LocalDateTime
- createdAtEnd: LocalDateTime
- pageNum: Integer = 1
- pageSize: Integer = 10
```

注：`doorPassword` / `emergencyContact` (secret) 不出现。

#### StudentExam
```
- studentExamCode: List<String>           [in]
- studentId: List<Long>                   [in]
- subject: List<SubjectEnum>              [in]
- score: List<BigDecimal>                 [in]
- examDateStart: LocalDate
- examDateEnd: LocalDate
- examTimeStart: LocalTime
- examTimeEnd: LocalTime
- submittedAtStart: LocalDateTime
- submittedAtEnd: LocalDateTime
- createdAtStart: LocalDateTime
- createdAtEnd: LocalDateTime
- pageNum: Integer = 1
- pageSize: Integer = 10
```

### 4.3 出参结构

```json
{
  "errorCode": null,
  "data": {
    "total": 100,          // 分页中的总条数
    "list": [
      {
        "studentProfileCode": "SP001",
        "idCard": "123456...",
        "studentName": "Alice",
        "bio": "...",
        "age": 20,
        "gender": "M",
        "createdAt": "2026-05-28 10:00:00",
        "updatedAt": "2026-05-28 11:00:00"
      },
      ...
    ]
  }
}
```

**后端骨架**：`RequestResult<PageResult<T>>`，其中 `PageResult.total` 和 `PageResult.list`。

**字段说明**：
- `${formName}Code`：业务主键，自动注入
- `createdAt` / `updatedAt`：审计字段
- secret 字段（如果 editPattern=userInput）**也不出现**在 list 出参中（预期后端脱敏或隐藏）

---

## 5. Save 接口

### 5.1 入参（新建）

| Form | 必填字段 | 可选字段 | 说明 |
|---|---|---|---|
| StudentProfile | idCard, studentName, age, gender | bio | 业务主键 studentProfileCode 不传（后端自动生成） |
| StudentDormitory | studentId, hasAllergy, monthlyRent | facilities | initPattern=todo 的字段（doorPassword, emergencyContact）根据后续 gap 决议 |
| StudentExam | studentId, subject, score, examDate, examTime, submittedAt | — | — |

### 5.2 入参（编辑）

append `studentProfileCode` 等业务主键字段（后端用来识别哪条记录），其余规则同新建但遵守 editPattern。

### 5.3 出参

```json
{
  "errorCode": null,
  "data": {
    "studentProfileCode": "SP_NEW_001"    // 仅返回业务主键
  }
}
```

---

## 6. GetDetail 接口

### 6.1 入参

```
- ${formName}Code: String (NotNull)       // 业务主键
```

### 6.2 出参

同 list 出参的单条格式，包含所有字段（包括 secret 字段——**待观察是否脱敏**）：

```json
{
  "errorCode": null,
  "data": {
    "studentProfileCode": "SP001",
    "idCard": "123456...",
    "studentName": "Alice",
    "bio": "...",
    "age": 20,
    "gender": "M",
    "createdAt": "2026-05-28 10:00:00",
    "updatedAt": "2026-05-28 11:00:00"
  }
}
```

---

## 7. Delete 接口

### 7.1 入参

```
- ${formName}Codes: List<String> (NotEmpty)   // 业务主键列表（支持批量删除）
```

**例**：`deleteStudentProfile` 的入参为 `{ "studentProfileCodes": ["SP001", "SP002"] }`

### 7.2 出参

```json
{
  "errorCode": null,
  "data": null
}
```

---

## 8. 枚举编码

所有 select / multiSelect 字段的枚举在 JSON 中以 **code 字符串** 序列化（Jackson `@JsonValue` 在 code 字段）。

| Form/Field | Enum Class | Values | 例 |
|---|---|---|---|
| StudentProfile.gender | GenderEnum | M / F | "M" 或 "F" |
| StudentDormitory.facilities | FacilitiesEnum | AC / HEATER / DESK | "AC" 或 ["AC", "HEATER"] |
| StudentExam.subject | SubjectEnum | MATH / ENG | "MATH" 或 "ENG" |

---

## 9. 时间字段格式

所有时间字段在 JSON 中为字符串（Spring `@JsonFormat`）：

| ItemType | JSON 格式 | Java 类型 | 例 |
|---|---|---|---|
| time/date | `yyyy-MM-dd` | LocalDate | "2026-05-28" |
| time/time | `HH:mm:ss` | LocalTime | "10:00:00" |
| time/dateTime | `yyyy-MM-dd HH:mm:ss` | LocalDateTime | "2026-05-28 10:00:00" |
| createdAt / updatedAt | `yyyy-MM-dd HH:mm:ss` | LocalDateTime | — |

所有时区均为 `Asia/Shanghai`。

---

## 10. 业务主键字段名规范

form-generator 自动注入的业务主键（第一个字段）命名为 `${lowerCamelFormName}Code`：

| Form | Code 字段名 |
|---|---|
| StudentProfile | studentProfileCode |
| StudentDormitory | studentDormitoryCode |
| StudentExam | studentExamCode |

---

## 11. Secret 字段观察

当前 super-dsl.yml 中：
- `StudentDormitory.doorPassword`：initPattern=userInput, editPattern=userInput
- `StudentDormitory.emergencyContact`：initPattern=todo, editPattern=doNot

**后端行为（实际未观察，待联调验证）**：
- secret 字段是否出现在 list 出参？（预期：不出现或脱敏）
- secret 字段是否出现在 detail 出参？（预期：出现且明文）
- secret 字段在 save 入参中是否必填？（取决于 initPattern=todo 的后端语义）

---

## 12. onOff 字段

`StudentDormitory.hasAllergy` (type=onOff, isNonVoid=true)

- List 入参：`hasAllergy: List<Boolean>`
- List 出参：`hasAllergy: Boolean`
- Save 入参：`hasAllergy: Boolean` (NotNull)

---

## 13. MultiSelect 字段

`StudentDormitory.facilities` (type=multiSelect, isNonVoid=false)

- List 入参：`facilities: List<FacilitiesEnum>`
- List 出参：`facilities: ???` (预期 List<FacilitiesEnum> 或 String with comma separator) **待观察**
- Save 入参：`facilities: List<String>` 或 `String`（根据后端实际）

---

## 14. InitPattern / EditPattern 的后端语义

- **doNot**：create 或 edit 时该字段不传，后端也不接受；前端应隐藏或 readonly
- **userInput**：前端可以传值
- **todo**：后端期望什么行为（是否必填、是否可传）？**待 gap 决议**

---

## 15. 索引信息

3 个 form 的索引定义（仅供参考，前端无需感知）：

| Form | Unique | Field(s) |
|---|---|---|
| StudentProfile | true | idCard |
| StudentDormitory | true | studentId |
| StudentExam | true | (studentId, subject, examDate) |

---

## 备注

- 所有日期时间在请求/响应中均为字符串，前端需要用相应的日期组件（Naive UI DatePicker / TimePicker）处理
- MultiSelect 的实际序列化形式（List vs 逗号分隔）需要在实际集成时验证
- Secret 字段的完整行为（list/detail 中的可见性、save 入参的必填性）需要在联调时验证并更新 gaps.md
