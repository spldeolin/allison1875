# handler-transformer 集成测试用例说明

本目录包含 handler-transformer 工具的全部集成测试用例。每个测试通过基类 `HandlerTransformerItBaseTest`
将测试资源拷贝到临时目录、改写配置为绝对路径，然后调用 `Bootstrap.main()` 执行 handler-transformer，最后在生成/改写的 Java
文件上进行断言。

handler-transformer 的核心职责是：将 Controller 中的 **init 块 DSL**（包含 `handler`、`desc`、`Req`/`Resp` 内部类声明）转换为完整的
`@PostMapping`/`@GetMapping` handler 方法，并生成对应的 ReqDTO、RespDTO、Service 接口、ServiceImpl 文件。

---

## 基本转换

### BasicPostItTest

- 验证标准 POST 场景的完整转换流程
- Controller 中的 init 块（`handler`、`desc`、`Req`、`Resp` 内部类）被移除
- 生成 `@PostMapping` handler 方法，方法名由 URL 推导（`/create-order` → `createOrder`）
- 生成 Req DTO 文件，保留字段和校验注解（`@NotBlank`、`@NotNull`）
- 生成 Resp DTO 文件，包含响应字段
- 生成 Service 接口和 ServiceImpl 实现类
- Controller 中注入了 Service 依赖

### BasicGetItTest

- 验证 `@GetUrlQuery` 标注的 Req 类触发 `@GetMapping` 生成
- handler 方法参数为 query params（不使用 `@RequestBody`）
- `@GetUrlQuery` 的 Req 不生成独立 Req DTO 文件
- Resp DTO 和 Service 正常生成

---

## Req/Resp 组合

### ReqOnlyItTest

- 验证 init 块只有 Req、无 Resp 的场景
- 生成带 `@RequestBody` 参数但返回 `void` 的 handler 方法
- 仅生成 Req DTO，不生成 Resp DTO

### RespOnlyItTest

- 验证 init 块只有 Resp、无 Req 的场景
- 生成无 `@RequestBody` 参数但有返回值的 handler 方法
- 仅生成 Resp DTO，不生成 Req DTO

### NoReqNoRespItTest

- 验证 init 块只有 `handler` + `desc`、无 Req/Resp 的场景
- 生成 `void` 返回、无参数的 handler 方法
- 不生成任何 DTO 文件
- Service 接口和 ServiceImpl 正常生成，方法返回 `void`

---

## 嵌套 DTO

### NestedDtoItTest

- 验证 Req/Resp 内嵌套子类（如 `Address`、`Logistics`）被提取为独立 DTO 文件
- 父 DTO 中替换为字段引用
- Req 侧的嵌套字段自动附加 `@Valid` 注解

### NestDtoListItTest

- 验证嵌套 DTO 标注 `@L` 注解时的集合化处理
- 父 DTO 中字段名被复数化（如 `Student` → `students`）
- 字段类型为 `List<XxxDTO>`

### NestDtoWithPageAnnotationItTest

- 验证嵌套 DTO 标注 `@P` 注解时，父 DTO 中字段类型为 `PageResult<XxxDTO>`、字段名被复数化
- 与 `nest-dto-list` 的 `@L` 分支互补

### NestDtoNameEndingWithDtoItTest

- 验证嵌套类名已以 `DTO` 结尾时（如 `AddressDTO`），生成的文件名不会重复追加 `DTO` 后缀
- 如生成 `AddressDTO.java` 而非 `AddressDTODTO.java`

### NestDtoCustomAnnotationsItTest

- 验证嵌套 DTO 上的自定义注解（非 `@L`/`@P`）被迁移到父 DTO 的字段上
- `@L`/`@P` 注解被过滤不迁移，其他注解正常迁移

---

## 特殊注解

### ListAnnotationItTest

- 验证 Resp 标注 `@L` 注解时，handler 和 Service 的返回类型为 `List<XxxResp>`

### PageAnnotationItTest

- 验证 Resp 标注 `@P` 注解时，handler 和 Service 的返回类型为 `PageResult<XxxResp>`

---

## DSL 变体

### NoDescItTest

- 验证 init 块缺少 `desc` 变量时，工具不报错，使用默认描述（「未指定描述」）完成转换

### HandlerAliasItTest

- 验证 init 块使用 `h` / `d` 别名代替 `handler` / `desc` 时，工具仍能正确解析
- 描述文本正确出现在生成的 handler Javadoc 中

### NoHandlerSkipItTest

- 验证 init 块不包含 `handler` 变量时，该 init 块被跳过
- Controller 保持不变，不生成任何 handler、Service 或 DTO 文件

### NoValidInitDecItTest

- 验证项目中所有 Controller 的 init 块均不包含有效 `handler` 变量时，工具正常结束并输出 warn
- 与 `no-handler-skip` 不同——此 case 有 init 块但无 `handler` 变量（如只有 `desc` 和 Req/Resp）

---

## 多 init 块 / 多 Controller

### MultipleInitDecsItTest

- 验证同一 Controller 包含多个 init 块时，每个都被独立转换
- 3 个 init 块分别生成 3 个 handler 方法、3 个 Service 接口+Impl、对应的 DTO 文件

### MultiControllerItTest

- 验证项目包含多个 Controller 文件时，每个都被独立检测和转换
- 各 Controller 分别生成自己的 handler、Service 和 DTO

### EnableOneServiceItTest

- 验证 `enableOneService=true` 配置的效果
- 同一 Controller 的多个 init 块共享一个 Service 接口+Impl（名称取自 Controller 名）
- 多个方法聚合在同一个 Service 文件中

---

## Controller 注解

### ControllerAnnotationItTest

- 验证使用 `@Controller`（而非 `@RestController`）标注的类也能被正确检测和转换
- `@Controller` 注解保留不变

### AnnotationResolveFailureItTest

- 验证 Controller 上的注解无法 resolve 时（如 classpath 中不存在的注解），该注解被跳过而非报错终止
- 工具正常完成转换，handler/Service/DTO 正常生成

---

## DTO 生成选项

### DatetimeFieldsItTest

- 验证 Req/Resp 中的日期时间字段自动附加 `@JsonFormat` 注解
- `Date` / `LocalDateTime` → `yyyy-MM-dd HH:mm:ss`
- `LocalDate` → `yyyy-MM-dd`
- `LocalTime` → `HH:mm:ss`

### NoLombokItTest

- 验证 `isDataModelWithoutLombok=true` 配置的效果
- 生成的 DTO 包含手写 getter/setter/toString/equals/hashCode 方法
- 不包含 `@Data`、`@Accessors` 等 Lombok 注解

---

## Service 层生成策略

### EnableOneServiceExistingFileItTest

- 验证 `enableOneService=true` 且 Service/ServiceImpl 文件已存在时，工具复用已有文件而非重新生成
- 转换后 Service 文件中包含已有方法 + 新追加的方法

### ServiceNameCollisionItTest

- 验证非 `enableOneService` 模式下，目标 Service 文件名已存在时触发自动 rename
- 预置同名 Service 文件，生成的 Service 文件名被重命名（如追加数字后缀）

### ServiceMethodNameDeduplicationItTest

- 验证同一 Service 中多个方法名冲突时自动重命名
- `enableOneService=true` + 预置含同名方法的 Service 文件

---

## DSL 解析边界

### DuplicateHandlerDeclItTest

- 验证 init 块中 `handler` 变量被声明两次时，只取第一次的值
- 后续声明被忽略并 warn

### NonStringLiteralHandlerItTest

- 验证 `handler` 变量的初始值不是字符串字面量时，该变量被忽略
- 如 `String handler = getUrl();`，该 init 块被跳过

### ExpansionVariablesItTest

- 验证 init 块中除 `handler`/`desc` 外的自定义字符串变量被收集到 expansion Map 中
- 转换正常完成不报错

---

## 异常与错误处理

### InvalidInitBodyMoreThanTwoCoidItTest

- 验证 init 块中包含 3 个以上内部类时，抛出异常
- 如 `class Req`、`class Resp`、`class Extra`，工具抛出 `IllegalArgumentException`

### InvalidInitBodyWrongClassNameItTest

- 验证 init 块中内部类名既不是 `Req` 也不是 `Resp` 时，抛出异常
- 如 `class Input`，工具抛出异常
