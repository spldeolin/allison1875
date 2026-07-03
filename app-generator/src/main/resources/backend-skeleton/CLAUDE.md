# 后端二次开发规则

本项目由 app-generator 生成骨架，由 form-generator 生成 CRUD 代码。所有代码均可修改，但推荐**新增代码优先于修改现有代码**。

## 技术栈

Spring Boot 2.7 · MyBatis · Lombok · Java 8 · MySQL

## 目录结构

```
src/main/java/{namespace}/
├── annotation/          ← 自定义注解（@WebApiAuth 等）
├── common/              ← 基础设施（详见下方）
├── config/              ← Spring 配置
├── controller/          ← REST 接口入口
├── filter/              ← Servlet Filter（认证鉴权）
├── property/            ← 配置属性类
├── service/             ← 业务接口（每个动作一个接口）
│   └── impl/            ← 业务实现
├── dto/
│   ├── req/             ← 请求 DTO
│   └── resp/            ← 响应 DTO
├── entity/              ← 数据库实体
├── mapper/              ← MyBatis Mapper 接口
├── design/              ← Design DSL（编译期查询设计，勿手动修改）
├── enums/               ← 业务枚举（含功能权限的 PermissionEnum）
├── util/                ← 工具类
├── webmvc/              ← 全局异常处理
├── mybatis/             ← 自定义 TypeHandler
└── trace/               ← 请求体日志
src/main/resources/
├── db/                  ← DDL
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

## 功能权限体系（RBAC）

本项目内置完整的 RBAC 功能权限系统。

### 权限枚举（PermissionEnum）

`enums/PermissionEnum.java` 由 app-generator 生成（非手动维护），包含：

- 每个表单 4 个权限点：`LIST_X`, `CREATE_X`, `UPDATE_X`, `DELETE_X`
- Role 表单额外的 `GRANT_PERMISSION`, `GRANT_ROLE`
- 内部枚举 `Group`：每个表单对应一个 group
- `baseOn` 字段：描述权限依赖关系（LIST 为基础权限，其余依赖 LIST）

### 鉴权机制

| 组件                   | 职责                                     |
|----------------------|----------------------------------------|
| `@WebApiAuth`        | 方法级注解，声明接口所需权限（多值为 OR 语义：拥有任一即通过）      |
| `UserPermissionInitializer` | 启动时引导 admin 用户和初始角色 + 扫描 Controller 建立 path→权限映射 |
| `ApiAuthFilter`      | 认证（JWT token）+ 鉴权（查 Registry，对比用户权限列表） |

鉴权流程：请求 → ApiAuthFilter 验证 token → 从 user_role + role_permission 聚合用户权限 → 查 UserPermissionInitializer
获取路径所需权限 → OR 匹配 → 通过/403。

### 授权 API

| 接口                                        | 用途                  |
|-------------------------------------------|---------------------|
| `POST /api/v1/role/grantPermissions`      | 全量覆盖式为角色授予权限        |
| `POST /api/v1/role/listRolePermissions`   | 查询角色已有权限列表          |
| `POST /api/v1/user/grantRoles`            | 全量覆盖式为用户授予角色        |
| `POST /api/v1/user/listUserRoles`         | 查询用户已有角色列表          |
| `POST /api/v1/permission/listPermissions` | 查询全部权限点（按 group 分组） |

### 关联表

- `role_permission`（role_id, permission_code）— 角色-权限多对多
- `user_role`（user_id, role_id）— 用户-角色多对多

### 系统初始化（UserPermissionInitializer）

启动时确保：admin 用户存在、3 个初始角色（系统管理员/业务员/观察员）绑定正确权限、admin 绑定系统管理员、新用户自动绑定观察员。系统分组通过硬编码
`SYSTEM_GROUPS = Set.of("USER", "ROLE")` 识别。

### 二次开发要点

- 新增接口如需权限控制，在方法上添加 `@WebApiAuth(PermissionEnum.XXX)` 即可
- save 类接口通常用 `@WebApiAuth({PermissionEnum.CREATE_X, PermissionEnum.UPDATE_X})`（OR 语义）
- 无需手动维护 PermissionEnum — 由 app-generator 根据表单列表自动生成
- 不要修改 `UserPermissionInitializer` 中角色名称，前端页面有对应的 hardcoded 引用

## 文件上传/下载设施

骨架内置通用文件上传/下载，供动态表单的 file 字段使用。

### 存储抽象

`FileStorage`（`service` 包，门面接口）定义流式存储契约，上传/下载代码仅依赖此接口，与具体后端解耦。`store`/`load` 均为流式语义（`InputStream`），避免大文件全量驻留内存。

| 类                  | 用途                                                                          |
|--------------------|-------------------------------------------------------------------------------|
| `FileStorage`      | `service` 包门面接口：`store(InputStream, long size, fileKey)` / `InputStream load(fileKey)` / `getBucket()` |
| `S3FileStorage`    | `storage` 包 S3 实现，由 `FileStorageConfig` 在 `S3Client` Bean 存在（bucket 非空）时装配（AWS SDK v2）   |
| `LocalFileStorage` | `storage` 包本地存储兜底，bucket 为空时装配，存到 `s3.localDir`                          |
| `FileStorageConfig` | `config` 包装配类：`fileStorage()` 方法依据 `s3Properties.bucket` 选择后端，非空时构建 S3Client 并返回 `S3FileStorage`，否则降级返回 `LocalFileStorage` |
| `S3Properties`     | `__APP_NAME__.s3.*` 配置（endpoint/region/bucket/accessKey/secretKey/pathStyleAccess/localDir） |
| `FileProperties`   | `__APP_NAME__.file.*` 配置（downloadTokenSecret/downloadTokenTtlSeconds）        |

流式约定：`load` 返回的 `InputStream` 由调用方在 try-with-resources 中关闭；下载时不预设 `Content-Length`，由 Servlet 容器按 chunked 传输。

S3 未配置（bucket 空）时自动降级为本地存储，业务代码无感知。

### 文件类别

`FileCategoryEnum`（实现 `BaseEnum<String>`）：

| code | 扩展名白名单 |
|------|------------|
| image / document / archive / audio / video | 各自扩展名白名单 |
| general | 反向黑名单（除 exe/bat/cmd/sh/js/jar/msi/com/scr/vbs/dll/app 外都放行） |

`isExtensionAllowed(ext)` 用于上传校验。不引入 Apache Tika，contentType 取 `MultipartFile.getContentType()`，兜底 `application/octet-stream`。

### 接口

| 接口 | 方法 | 鉴权 | 说明 |
|------|------|------|------|
| `/api/v1/file/uploadFile` | POST multipart | 需 token | 通用上传，参数 `file` + `category`，返回 `{ fileKey, originFileName }` |
| `/api/v1/file/temporarilyDownloadFile` | POST | 需 token | 传 `fileKey`，签发有时效的下载令牌 `{ token }` |
| `/api/v1/file/downloadFile?token=` | GET | **匿名**（已加入 `anonymousApiPaths`） | 流式返回文件，`Content-Disposition: inline` 供内联预览 |

下载接口匿名是因为浏览器原生 GET（`<img src>`/`<iframe src>`）无法带 Authorization 头。

### 无状态下载令牌

`DownloadTokenUtils`（HMAC-SHA256）：`token = base64url(payload) + "." + base64url(hmac)`，`payload = fileKey + "|" + expireAt`。无状态、多节点无共享存储，代价是签发后无法主动吊销（等过期）。密钥来自 `__APP_NAME__.file.downloadTokenSecret`，DSL 未指定时 app-generator 生成随机密钥。

### file_record 表

上传即落库的完整登记表（`FileRecordEntity`/`FileRecordMapper` 已生成）：`id/fileKey(UK)/originFileName/contentType/fileSize/category/bucket/createdAt/createdBy`。下载接口按 `fileKey` 等值精确查询。表不可变（无 updated_at/updated_by）。

### 占位符

app-generator 生成时替换：`__S3_ENDPOINT__`、`__S3_REGION__`、`__S3_BUCKET__`、`__S3_ACCESS_KEY__`、`__S3_SECRET_KEY__`、`__FILE_DOWNLOAD_TOKEN_SECRET__`。

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
