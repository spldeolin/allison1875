# 后端二次开发规则

本项目由 app-generator 生成骨架，由 form-generator 生成 CRUD 代码。所有代码均可修改，但推荐**新增代码优先于修改现有代码**。

## 技术栈

Spring Boot 2.7 · MyBatis · Lombok · Java 8 · MySQL

## 目录结构

```
src/main/java/{namespace}/
├── common/              ← 基础设施（详见下方）
├── config/              ← Spring 配置
├── controller/          ← REST 接口入口
├── service/             ← 业务接口（每个动作一个接口）
│   └── impl/            ← 业务实现
├── dto/
│   ├── req/             ← 请求 DTO
│   └── resp/            ← 响应 DTO
├── entity/              ← 数据库实体
├── mapper/              ← MyBatis Mapper 接口
├── design/              ← Design DSL（编译期查询设计，勿手动修改）
├── enums/               ← 业务枚举
├── util/                ← 工具类
├── webmvc/              ← 全局异常处理
├── mybatis/             ← 自定义 TypeHandler
└── trace/               ← 请求体日志
src/main/resources/
├── mapper/              ← MyBatis XML
├── application.yml
└── logback-spring.xml
```

## 核心设施

| 类                  | 用途                                                                                 |
|--------------------|------------------------------------------------------------------------------------|
| `RequestResult<T>` | 统一响应包装。成功 `RequestResult.success(data)`，失败 `RequestResult.failure(errorCode, msg)` |
| `PageResult<T>`    | 分页响应。有数据 `PageResult.of(total, list)`，无数据 `PageResult.empty()`                     |
| `BizException`     | 业务异常，被 GlobalExceptionAdvice 捕获后返回给前端                                              |
| `ErrorCode`        | 错误码接口。自定义错误码实现此接口即可                                                                |
| `BaseEnum<C>`      | 枚举基类，配合 EnumTypeHandlerEx 实现 DB↔枚举自动转换                                             |
| `JsonUtils`        | Jackson 序列化/反序列化工具                                                                 |
| `TimeUtils`        | LocalDateTime/LocalDate 格式化工具                                                      |

## 已生成代码的模式

每个表单生成 4 个接口，遵循统一模式：

- **Controller**: `/api/v1/{formName}/save|list|getDetail|delete`，全部 POST
- **Service**: 每个动作独立接口 + 实现（如 `SaveOrderService` / `SaveOrderServiceImpl`）
- **DTO**: 请求 `{Action}{FormName}Req`，响应 `{Action}{FormName}Resp`
- **Entity**: 与 DB 表一一映射，含 `createdAt`/`updatedAt`
- **Mapper**: 接口 + XML，基础 CRUD 方法已生成
- **bizKey**: `{formName}Code`（UUID 字符串），贯穿前后端

## 二次开发指导原则

### 推荐：新增

- **新增接口**：在已有 Controller 中添加方法，或创建新 Controller
- **新增 Service**：创建新的 Service 接口 + Impl
- **新增 Mapper 方法**：在 Mapper 接口添加方法，在 XML 的 `[END]` 标记之后添加 SQL
- **新增 DTO**：在 `dto/req/` 或 `dto/resp/` 下创建
- **新增工具类**：在 `util/` 下创建

### 不推荐：修改已生成代码

已生成的 ServiceImpl 包含完整的 CRUD 逻辑。如需定制：

- **增强而非替换**：在已有 ServiceImpl 方法前后添加逻辑，而非重写核心流程
- **新建 Service 调用已有 Service**：组合优于修改

### 不可修改（会被工具覆盖）

- `design/*.java` — Design DSL，由 allison1875 工具生成
- `entity/*.java` — Entity 类，由工具生成
- Mapper XML 中 `[START]...[END]` 标记之间的内容

## 新增接口的标准做法

1. 在 Controller 中添加方法
2. 创建 Req/Resp DTO
3. 创建 Service 接口 + Impl
4. 如需新 SQL，在 Mapper 接口添加方法，XML `[END]` 标记后添加实现

遵循项目约定：全部 POST、RequestResult 包装响应、BizException 抛业务错误。

## 响应格式约定

```json
{
    "errorCode": null,
    "data": {},
    "errorMsg": null,
    "traceId": "xxx"
}
```

- 成功：`errorCode` 为 null
- 失败：`errorCode` 非 null，`errorMsg` 包含用户可见信息

## 枚举使用

实现 `BaseEnum<C>` 接口，DB 存 code 值，JSON 序列化/反序列化自动转换：

```java
public enum StatusEnum implements BaseEnum<String> {
    ACTIVE("active", "活跃"), INACTIVE("inactive", "停用")
    // getCode() + getTitle()
}
```
