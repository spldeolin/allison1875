# 文件组件设计（app-generator 文件字段）

日期：2026-07-01
分支：14.0

## 背景与目标

app-generator 引入「文件组件」——动态表单的一种字段类型。目标是让 app.yml
DSL 中可声明 `file` 类型字段，生成的前后端一体应用即具备文件上传、存储、预览、下载能力。

跨三个层次：

1. **form-generator** 新增 `file` itemType，一个 DSL item 生成两个 DB 列与两个 DTO 字段
2. **app-generator 后端骨架** 引入对象存储（S3，可降级本地）+ 文件上传/下载接口 + 文件记录表
3. **app-generator 前端骨架** 新增 naive-ui 风格文件组件，两步上传 + 内联预览

## 已确认的关键决策

| # | 决策 | 选择 |
|---|------|------|
| 1 | 文件记录表职责 | 上传即落库的**完整登记表**（file_record） |
| 2 | 前端获取文件字节方式 | **后端代理下载接口**，不暴露 S3 凭证/URL |
| 3 | 对象存储 SDK | **AWS SDK v2**（`software.amazon.awssdk:s3`） |
| 4 | 上传接口形态 | **通用接口 + category 参数**（方案 1，非每字段独立接口） |
| 5 | 文件类别 accept | **DSL 枚举**（image/document/archive/audio/video/general），general 为安全扩展名兜底 |
| 6 | S3 未配置时 | **降级为本地存储** |
| 7 | 下载令牌 | **无状态 HMAC-SHA256 签名令牌**（多节点无共享存储） |
| 8 | 业务表 file 列 | **合并单列** `fileKey/originFileName`（首个 `/` 分隔）；file_record 表仍保留独立两列 |

## 数据层：file_record 表（已生成持久层）

DDL（已在 `backend-skeleton/sql/ddl.sql`，持久层 Entity/Mapper/XML 已生成）：

```sql
CREATE TABLE `file_record`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_key`         VARCHAR(255) NOT NULL COMMENT '文件Key（uuid+扩展名），业务唯一键，业务表引用此值',
    `origin_file_name` VARCHAR(255) NOT NULL COMMENT '上传时的原始文件名',
    `content_type`     VARCHAR(128) NOT NULL COMMENT 'MIME类型，下载/预览时回填Content-Type',
    `file_size`        BIGINT       NOT NULL COMMENT '文件大小（字节）',
    `category`         VARCHAR(32)  NOT NULL COMMENT '上传时的文件类别（image/document/general等）',
    `created_at`       DATETIME     NOT NULL COMMENT '创建时间',
    `created_by`       VARCHAR(32) COMMENT '创建人',
    UNIQUE KEY `uk_file_key` (`file_key`),
    PRIMARY KEY (`id`)
) COMMENT '文件记录';
```

已生成契约：
- `FileRecordEntity`：`id/fileKey/originFileName/contentType/fileSize:Long/category/createdAt/createdBy`
- `FileRecordMapper`：`insert(entity)`、`queryByFileKey(fileKey)` 等
- 表不可变（无 updated_at/updated_by）；file_key 即业务唯一键，无冗余 code 列

## 第一部分：后端依赖、配置与存储降级

### pom.xml

新增 AWS SDK v2：
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.46.17</version>
</dependency>
```

### application.yml

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 200MB      # 全局硬上限，兜底
      max-request-size: 200MB

__APP_NAME__:
  s3:
    endpoint: __S3_ENDPOINT__
    region: __S3_REGION__
    bucket: __S3_BUCKET__
    accessKey: __S3_ACCESS_KEY__
    secretKey: __S3_SECRET_KEY__
    pathStyleAccess: true       # 默认 true，兼容 MinIO
    localDir: ./file-storage    # bucket 为空时的本地存储目录
  file:
    downloadTokenSecret: __FILE_DOWNLOAD_TOKEN_SECRET__
    downloadTokenTtlSeconds: 300
```

### Config 类（common 模块）

新增可选字段（遵循 wiring 步骤：字段声明 + `applyDefaults()`）：

| 字段 | 默认值（applyDefaults） |
|------|--------------------|
| `s3Endpoint` | 空串                 |
| `s3Region` | 空串                 |
| `s3Bucket` | 空串                 |
| `s3AccessKey` | 空串                 |
| `s3SecretKey` | 空串                 |
| `fileDownloadTokenSecret` | 不能为空               |

`s3Bucket` 为空 → 视为「未配置 S3」→ 后端降级本地存储。

### AppGenerator.generateBackend()

新增 6 个 `replaceInAllFiles` 占位符替换：`__S3_ENDPOINT__`、`__S3_REGION__`、
`__S3_BUCKET__`、`__S3_ACCESS_KEY__`、`__S3_SECRET_KEY__`、`__FILE_DOWNLOAD_TOKEN_SECRET__`。

### 新增骨架文件

- `property/S3Properties.java` — `@ConfigurationProperties(prefix="__APP_NAME__.s3")`，含 `pathStyleAccess`、`localDir`，风格同 `AuthcProperties`
- `config/S3Config.java` — 仅当 `bucket` 非空时构建单例 `S3Client` Bean（`@ConditionalOnProperty`），endpoint 非空时 override，启用 path-style access
- `storage/FileStorage.java`（接口）— `store(byte[] bytes, String fileKey)` / `byte[] load(String fileKey)`
- `storage/S3FileStorage.java` — `@ConditionalOnProperty(name="__APP_NAME__.s3.bucket")`，依赖 S3Client
- `storage/LocalFileStorage.java` — `@ConditionalOnMissingBean(FileStorage.class)` 兜底，存到 `localDir`

上传/下载代码只依赖 `FileStorage` 接口，与具体后端解耦。

## 第二部分：上传接口 + 文件类别枚举

### enums/FileCategoryEnum.java（新建，实现 BaseEnum<String>）

| code | title | 扩展名白名单 |
|------|-------|------------|
| `image` | 图片 | png, jpg, jpeg, gif, webp, bmp, svg |
| `document` | 文档 | pdf, doc, docx, xls, xlsx, ppt, pptx, txt, csv, md |
| `archive` | 压缩包 | zip, rar, 7z, tar, gz |
| `audio` | 音频 | mp3, wav, flac, aac, ogg |
| `video` | 视频 | mp4, avi, mov, mkv, webm |
| `general` | 普通文件 | 上述所有 + 其它安全扩展名（反向黑名单实现） |

- 方法：`getExtensions() → Set<String>`、`isExtensionAllowed(ext)`
- `general` 用**反向黑名单**：除危险扩展名外都放行。黑名单：
  `exe, bat, cmd, sh, js, jar, msi, com, scr, vbs, dll, app`
- **不引入 Apache Tika**；contentType 取 `MultipartFile.getContentType()`，兜底 `application/octet-stream`

### 上传接口

新增 `FileController` + `FileService` + `FileServiceImpl`：

```java
@RestController
@RequestMapping("/api/v1/file")
public class FileController {
    @PostMapping("uploadFile")
    public RequestResult<UploadFileResp> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) { ... }
}
```

`UploadFileResp`：`{ fileKey, originFileName }`

`FileServiceImpl.upload()` 流程：
1. 校验 `category` 合法（转 `FileCategoryEnum`，非法抛 `BizException(BAD_REQUEST)`）
2. 提取原始文件名扩展名（小写）
3. `category.isExtensionAllowed(ext)` 校验，不通过抛 `BizException`
4. `fileKey = UuidUtils.generateShort() + "." + ext`
5. contentType = `MultipartFile.getContentType()`，兜底 `application/octet-stream`
6. `fileStorage.store(bytes, fileKey)`
7. 组装 `FileRecordEntity`（createdAt=now，createdBy=`CurrentUser.getUsernameOrDefault("system")`）→ `fileRecordMapper.insert()`
8. 返回 `{ fileKey, originFileName }`

鉴权：上传接口不加 `@WebApiAuth`（通用接口，非表单权限点），但仍在 `/api/v1/**` 下 → 需 token 认证。

大小校验：全局 200MB 由 Spring 兜底；**每字段 maxFileSize 精确校验放前端**（后端通用接口无表单字段上下文）。

## 第三部分：无状态 HMAC 签名下载令牌

### 问题

`ApiAuthFilter` 强制 `/api/v1/**` 携带 Authorization 头。前端预览时图片走
`<img src>`、PDF 走 `<iframe src>`，浏览器原生 GET 无法带头 → 401。

### 令牌方案（无状态，多节点无共享存储）

```
token = base64url(payload) + "." + base64url(HMAC-SHA256(secret, payload))
payload = fileKey + "|" + expireAtEpochMilli
```

密钥来自 `application.yml` 的 `__APP_NAME__.file.downloadTokenSecret`（Config 占位符注入，
未指定则生成期用 `SecretKeyUtils.generateUrlSafeKey(64)` 生成）。同一份生成产物密钥一致 →
多节点验签通过。

权衡：无状态换来多节点无共享存储，代价是签发后无法主动吊销（等过期）。5 分钟 TTL 风险可接受。

### util/DownloadTokenUtils.java（新建工具类，私有构造抛异常）

- `sign(fileKey, secret, ttlSeconds) → token`：HMAC-SHA256，拼 payload.signature
- `verify(token, secret) → fileKey`：拆分 → 重算签名比对（`MessageDigest.isEqual` 防时序攻击）→ 校验未过期 → 返回 fileKey；失败抛 `BizException(BAD_REQUEST)`

### 两个接口

- **POST** `/api/v1/file/temporarilyDownloadFile` — 正常鉴权，传 `fileKey` → 校验
  `file_record` 存在 → `DownloadTokenUtils.sign(...)` → 返回 `{ token }`
- **GET** `/api/v1/file/downloadFile?token=xxx` — **加入 `anonymousApiPaths` 白名单** →
  `verify` 得 fileKey → 查 `file_record` 拿 originFileName/contentType →
  `fileStorage.load(fileKey)` → 流式写 `HttpServletResponse`：
  `Content-Type`=contentType，`Content-Disposition: inline; filename*=UTF-8''{urlEncode(originFileName)}`
  （`inline` 使图片/PDF 浏览器内联预览）

application.yml 现有 `anonymousApiPaths` 追加 `,/api/v1/file/downloadFile`。

## 第四部分：form-generator 新增 file itemType

### 合并列设计（一 item = 一列 = 一字段）

**关键简化**：业务表的 file 字段不拆两列，而是**合并为一个 VARCHAR 列**，值为
`fileKey/originFileName`（用**首个 `/`** 分隔）。

- fileKey = uuid + 扩展名，不含 `/`；文件名各 OS 均禁用 `/` 作路径分隔符。故首个 `/`
  拆分**永远无歧义**。
- 因此 `file` 类型对 form-generator 而言退化成「就是一个 VARCHAR 列 + 一个 String DTO
  字段」，**不打破「一 item 一列」假设**，无需任何双列/双字段特殊分支。
- 列长度 **VARCHAR(512)**（uuid+ext ~40 + `/` + 文件名最多 255，留余量）。
- 前端 `FileValue {fileKey, originFileName}` 提交时 join 成 `fileKey + '/' + originFileName`；
  取回时按首个 `/` split。

**为何 file_record 表不合并**：file_record（骨架固定表，持久层已生成）需按 fileKey
**等值精确查询**（下载接口），保留显式 `file_key` 列 + `uk_file_key` 唯一索引 + `= ?`
查询是最优解——避免 LIKE 的 `_` 通配符转义问题、保留唯一约束、语义清晰。合并只用于
**业务表**，file_record **保持不变**。

**权衡备注（文件名搜索）**：现设计中 file 字段不可搜索。若将来需按文件名搜索业务实体，
应在 file_record 表（有独立 `origin_file_name` 列）上做，再 join fileKey 回业务表，
而非直接搜业务表的合并列（originFileName 在合并列后缀，只能前导通配符 `%..%` 全表扫）。
注：文件名模糊搜索本身天生 non-sargable，合并与否都是全表扫描，合并未额外损失此能力。

### DSL

```yaml
- type: file
  name: attachment
  title: 附件
  isNonVoid: true
  category: document      # FileCategoryEnum 之一，默认 general
  maxFileSize: 10         # 前端 UX 校验用（MB），可空，留空不限制
```

### 新增/修改文件

| # | 文件 | 动作 |
|---|------|------|
| 1 | `dsl/enums/ItemType.java` | 加 `FILE("file")` |
| 2 | `dsl/ItemDef.java` | `@JsonSubTypes` 加 `FileItemDef` |
| 3 | `dsl/item/FileItemDef.java`（新建） | 字段 `category`（默认 general）、`maxFileSize`（可空）；`validate()` 校验 category 合法 |
| 4 | `service/impl/PrimaryItemServiceImpl.java` | 注入 + `case FILE` 委托 |
| 5 | `service/impl/FileItemService.java`（新建） | `ItemService<FileItemDef>`：`getDbColumnType` 返回 `VARCHAR(512)`、`getJavaTypeInDTO` 返回 `String`；不可过滤/不可排序 |

**关键**：因合并为单列单字段，file 走**标准单字段路径**，`DdlServiceImpl` /
`CreateApiServiceImpl` / `UpdateApiServiceImpl` / `GetDetailApiServiceImpl` /
`ListApiServiceImpl` / `MutationApiSupport` **无需 FILE 特殊分支**——只要 `FileItemService`
的单字段接口方法返回正确值即可。仅需确保：list 过滤跳过 file（不可搜索）、SortEnum 不含 file。

- **Entity 字段**：persistence-generator 读 DDL 自动生成一个 String 字段（VARCHAR(512)），form-generator 无需干预
- **排序枚举**：file 不进 SortEnum
- **列名/字段名**：DB 列 `{name}`（snake_case）、DTO/Entity 字段 `{name}`，值为 `fileKey/originFileName`

### app-generator 审计日志联动

`AppGeneratorMutationExpansionServiceImpl` 构建 `auditContent` Map 时，file 字段值取
`originFileName`（合并列首个 `/` 之后的部分，可读），不取 fileKey。实现时需确认其现有遍历逻辑对 file 类型的处理。

## 第五部分：前端文件组件（frontend-skeleton）

按 `docs/file-component/approved-visual-mockup.html` 的**已批准视觉**实现，naive-ui 风格，四态齐全。

> 注：mockup.html 仅为**大致批准**，前端 UI/UX 在实现阶段可能还需迭代调整，不作像素级冻结规范。

### 类型层 src/schema/types.ts

- `FileItemDef`：`{ type: 'file', category, maxFileSize? }`
- `FileValue`：`{ fileKey: string, originFileName: string }` — 表单 state 中 file 字段持有此对象

### src/core/fields/file-category.ts（新建）

前端镜像后端 `FileCategoryEnum` 的扩展名白名单，用于生成 `<input accept>` 与提示文案：
`acceptOf(category)`、`hintOf(category, maxFileSize)`。

### API 层（src/core/protocol/file-api.ts 新建或并入 request.ts）

- `uploadFile(file, category) → { fileKey, originFileName }` — POST multipart 到 `/api/v1/file/upload`
- `fetchDownloadToken(fileKey) → token` — POST `/api/v1/file/temporarilyDownloadFile`
- `downloadUrlOf(token) → '/api/v1/file/downloadFile?token=xxx'`

### 组件 src/core/fields/FileField.vue（新建）四态

1. **编辑·未上传**：`NUploadDragger` 拖拽区，提示来自 `hintOf(category, maxFileSize)`
2. **编辑·已上传**：文件卡片（图标+文件名省略+预览/移除），移除清空 FileValue
3. **列表/详情展示**：紧凑单元格（图标+文件名+预览眼睛），只读
4. **预览弹框**：`NModal`；先 `fetchDownloadToken` 拿 token → 图片用 `NImage`、PDF 用
   `<iframe>`、其它仅「下载」按钮。**图片/PDF/其它的判定依据 `originFileName` 扩展名**（纯前端，无需探测响应头）

### 上传两步走

选文件 → 前端校验 accept + maxFileSize → `uploadFile` 得 `{fileKey, originFileName}`
存入 FileValue → 表单提交时 `request-builder` 将 FileValue **join 成单个字符串**
`fileKey + '/' + originFileName` 写入 `{name}` 字段；取回（detail/list）时按**首个 `/`**
split 还原为 FileValue。

### 接线

- `request-builder.ts`：file 字段 join/split 单列（`fileKey + '/' + originFileName` ↔ 首个 `/` 拆分），而非拆两列
- `field-policy.ts`：file 类型标记不可搜索
- `FieldRenderer.vue`：注册 `file` → `FileField.vue`

## 实现顺序（按需求工作流）

1. ✅ file_record DDL 设计 + 持久层生成（已完成）
2. 后端：S3 依赖/配置/存储降级 → 上传接口 → 下载令牌接口
3. form-generator：file itemType
4. 前端：文件组件
5. super DSL 中每个表单条目的文件字段联调（用户负责）

## 文档维护

按根 CLAUDE.md「维护 CLAUDE.md」规则，本特性完成后需同步更新：
- `form-generator/CLAUDE.md` — 新增 `file` 字段类型（DSL 字段 category/maxFileSize、合并单列 `fileKey/originFileName`）
- `app-generator/backend-skeleton/CLAUDE.md` — 文件上传/下载/存储设施
- `skills/integrate-allison1875/SKILL.md` — Config 新增 5 个 S3 字段 + downloadTokenSecret

## 不做（YAGNI）

- 不引入 Apache Tika（魔数嗅探）
- 不做令牌主动吊销（无状态签名，等过期）
- 不做每字段独立上传接口（方案 2 已排除）
