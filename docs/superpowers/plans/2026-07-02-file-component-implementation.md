# File Component Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `file` field type to app-generator so a dynamic form can declare a file field, and the generated fullstack app gains file upload (S3/local), stateless HMAC-signed download tokens, inline preview, and a naive-ui four-state file component.

**Architecture:** Three subsystems sharing two contracts: (1) `FileCategoryEnum` — six categories with extension whitelists, `general` uses a reverse blacklist; (2) the merged business-table column format `fileKey/originFileName` (first `/` splits). Backend skeleton gets storage (FileStorage interface + S3/Local impls), upload + token-sign + download endpoints. form-generator adds `FILE` itemType as a plain single-column single-field item (no special-case branching). Frontend gets a `FileField.vue` four-state component + `file-api.ts` + `file-category.ts` + wiring in request-builder/field-policy/FieldRenderer.

**Tech Stack:** AWS SDK v2 (s3 2.46.17) · Spring Boot 2.7 / Java 8 (skeleton — `@Resource`/`@Valid`/validation annotations are legal here) · Google Guice + JavaParser (tool source — NO validation annotations) · naive-ui 2.41 · Vue 3 + TypeScript.

**Reference spec:** `docs/superpowers/specs/2026-07-01-file-component-design.md`
**Visual reference:** `docs/file-component/approved-visual-mockup.html` (roughly approved; UI/UX may iterate)

**Phase 0 done:** file_record DDL + Entity/Mapper/XML generated (commit f12ff18a). `FileRecordEntity` fields: `id/fileKey/originFileName/contentType/fileSize:Long/category/bucket/createdAt/createdBy`. `bucket` is nullable (local-storage mode). `FileRecordMapper.queryByFileKey(fileKey)` exists.

---

## File Structure

### Backend skeleton (`app-generator/src/main/resources/backend-skeleton/`)
All paths below are relative to that root. Placeholders `__NAMESPACE__`, `__NAMESPACE_PATH__`, `__APP_NAME__` are replaced at generation time.

| File | Action | Responsibility |
|------|--------|----------------|
| `pom.xml` | Modify | Add AWS SDK v2 `s3` dependency |
| `src/main/resources/application.yml` | Modify | Add `spring.servlet.multipart` limits, `__APP_NAME__.s3.*`, `__APP_NAME__.file.*`, append downloadFile to `anonymousApiPaths` |
| `src/main/java/__NAMESPACE_PATH__/property/S3Properties.java` | Create | `@ConfigurationProperties(prefix="__APP_NAME__.s3")` — endpoint/region/bucket/accessKey/secretKey/pathStyleAccess/localDir |
| `src/main/java/__NAMESPACE_PATH__/property/FileProperties.java` | Create | `@ConfigurationProperties(prefix="__APP_NAME__.file")` — downloadTokenSecret/downloadTokenTtlSeconds |
| `src/main/java/__NAMESPACE_PATH__/config/S3Config.java` | Create | `S3Client` bean, `@ConditionalOnProperty(prefix="__APP_NAME__.s3", name="bucket")` |
| `src/main/java/__NAMESPACE_PATH__/storage/FileStorage.java` | Create | Interface: `store(bytes, fileKey)` / `byte[] load(fileKey)` / `String getBucket()` |
| `src/main/java/__NAMESPACE_PATH__/storage/S3FileStorage.java` | Create | `@ConditionalOnProperty` S3 impl |
| `src/main/java/__NAMESPACE_PATH__/storage/LocalFileStorage.java` | Create | `@ConditionalOnMissingBean(FileStorage.class)` local impl |
| `src/main/java/__NAMESPACE_PATH__/enums/FileCategoryEnum.java` | Create | 6 categories, `BaseEnum<String>`, extension whitelist + `general` reverse blacklist |
| `src/main/java/__NAMESPACE_PATH__/util/DownloadTokenUtils.java` | Create | Stateless HMAC-SHA256 sign/verify, private constructor |
| `src/main/java/__NAMESPACE_PATH__/controller/FileController.java` | Create | uploadFile / temporarilyDownloadFile / downloadFile |
| `src/main/java/__NAMESPACE_PATH__/service/FileService.java` | Create | Interface |
| `src/main/java/__NAMESPACE_PATH__/service/impl/FileServiceImpl.java` | Create | Upload + token-sign + download-stream logic |
| `src/main/java/__NAMESPACE_PATH__/dto/resp/UploadFileResp.java` | Create | `{ fileKey, originFileName }` |
| `src/main/java/__NAMESPACE_PATH__/dto/resp/TemporarilyDownloadFileResp.java` | Create | `{ token }` |

### Tool source (common + app-generator)
| File | Action | Responsibility |
|------|--------|----------------|
| `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java` | Modify | Add 6 fields + applyDefaults defaults |
| `app-generator/src/main/java/.../AppGenerator.java` | Modify | Add 6 placeholder replacements in `generateBackend()` |

### form-generator
| File | Action | Responsibility |
|------|--------|----------------|
| `form-generator/src/main/java/.../dsl/enums/ItemType.java` | Modify | Add `FILE("file")` |
| `form-generator/src/main/java/.../dsl/ItemDef.java` | Modify | Add `FileItemDef` to `@JsonSubTypes` |
| `form-generator/src/main/java/.../dsl/item/FileItemDef.java` | Create | `category` (default general), `maxFileSize` (nullable); `validate()` |
| `form-generator/src/main/java/.../service/impl/FileItemService.java` | Create | `ItemService<FileItemDef>` — VARCHAR(512), String DTO, not filterable, not sortable |
| `form-generator/src/main/java/.../service/impl/PrimaryItemServiceImpl.java` | Modify | Inject + `case FILE` delegation |
| `app-generator/src/main/java/.../service/impl/AppGeneratorMutationExpansionServiceImpl.java` | Modify | file field audit takes originFileName (split merged column) |

### Frontend skeleton (`app-generator/src/main/resources/frontend-skeleton/src/`)
| File | Action | Responsibility |
|------|--------|----------------|
| `schema/types.ts` | Modify | Add `FileItemDef`, `FileValue` |
| `core/fields/file-category.ts` | Create | Mirror FileCategoryEnum whitelists; `acceptOf`, `hintOf` |
| `core/protocol/file-api.ts` | Create | uploadFile / fetchDownloadToken / downloadUrlOf |
| `core/fields/FileField.vue` | Create | Four-state component |
| `core/protocol/request-builder.ts` | Modify | file join/split merged column |
| `core/protocol/field-policy.ts` | Modify | file not searchable |
| `core/fields/FieldRenderer.vue` | Modify | Register `file` → FileField |

---

## Phase 1: Backend storage + upload + download

### Task 1: Add AWS SDK v2 dependency to backend pom.xml

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/pom.xml`

- [ ] **Step 1: Add the s3 dependency**

In `app-generator/src/main/resources/backend-skeleton/pom.xml`, find the `<!-- Utils -->` block (contains guava, commons-lang3, commons-io, jbcrypt). Insert the AWS SDK v2 dependency immediately before the `<!-- Project-specific -->` comment block:

```xml
        <!-- Object storage -->
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
            <version>2.46.17</version>
        </dependency>
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/pom.xml
git commit -m "build: add AWS SDK v2 s3 dependency to backend skeleton

1. Add software.amazon.awssdk:s3 2.46.17 for object storage support"
```

---

### Task 2: Add Config fields for S3 + download token secret

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java`

- [ ] **Step 1: Add 6 field declarations**

In `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java`, locate the field declaration block (the area with `jdbcUrl`/`userName`/`password`/`schema`). Add these 6 fields in a contiguous block (place them after the `schema` field, before whatever follows it):

```java
    /**
     * S3对象存储端点，留空时使用SDK默认。bucket留空则整体降级为本地存储
     */
    String s3Endpoint;

    /**
     * S3对象存储区域
     */
    String s3Region;

    /**
     * S3对象存储桶名，留空则降级为本地存储
     */
    String s3Bucket;

    /**
     * S3对象存储访问密钥
     */
    String s3AccessKey;

    /**
     * S3对象存储私钥
     */
    String s3SecretKey;

    /**
     * 文件下载令牌签名密钥，不可为空
     */
    String fileDownloadTokenSecret;
```

- [ ] **Step 2: Add defaults in applyDefaults()**

Locate the `applyDefaults(Config raw)` method. In the section where defaults are set via `raw.toBuilder()...`, add these defaults (place near the other String-defaulting lines; if there is a `builder` variable, add to its chain — match the existing style). The pattern is:

```java
        if (raw.getS3Endpoint() == null) {
            builder.s3Endpoint("");
        }
        if (raw.getS3Region() == null) {
            builder.s3Region("");
        }
        if (raw.getS3Bucket() == null) {
            builder.s3Bucket("");
        }
        if (raw.getS3AccessKey() == null) {
            builder.s3AccessKey("");
        }
        if (raw.getS3SecretKey() == null) {
            builder.s3SecretKey("");
        }
```

If the method uses a single chained `builder = raw.toBuilder().xxx(...).build()` style instead of the `if (raw.getX()==null) builder.x(default)` style, match whatever style is actually present in the file (read it first). Do NOT default `fileDownloadTokenSecret` — it must be non-null; that validation is added in Task 3.

- [ ] **Step 3: Compile**

Run: `mvn -q -pl common compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/config/Config.java
git commit -m "feat: add S3 and download token secret fields to Config

1. Add s3Endpoint/s3Region/s3Bucket/s3AccessKey/s3SecretKey/fileDownloadTokenSecret fields
2. Default the five S3 fields to empty string in applyDefaults()"
```

---

### Task 3: Add fileDownloadTokenSecret validation to ConfigValidator

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValidator.java` (find exact path — `grep -rl "ConfigValidator" common/src/main/java`)

- [ ] **Step 1: Locate the validator and read it**

Run: `grep -rl "class ConfigValidator" common/src/main/java`
Read the file to see the existing validation pattern (e.g. how `jdbcUrl` non-null is enforced, what exception type is thrown).

- [ ] **Step 2: Add non-null validation for fileDownloadTokenSecret**

Add a validation check matching the existing pattern. Conceptually:

```java
        if (config.getFileDownloadTokenSecret() == null || config.getFileDownloadTokenSecret().isEmpty()) {
            // throw the same exception type used for other required-field validations,
            // with a message like "fileDownloadTokenSecret must not be null"
        }
```

Match the exact exception type and message style used by neighboring checks. If the validator does not currently enforce non-null on any field (only logs warnings), then add a check that throws `Allison1875Exception` (the project's non-generic exception — confirm its FQN by `grep -rn "class Allison1875Exception" common/`).

- [ ] **Step 3: Compile**

Run: `mvn -q -pl common compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValidator.java
git commit -m "feat: require fileDownloadTokenSecret in Config validation

1. Reject null/empty fileDownloadTokenSecret at config load time"
```

---

### Task 4: Wire 6 placeholder replacements in AppGenerator.generateBackend()

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Add the 6 replacements**

In `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`, in `generateBackend()`, locate the block of existing `replaceInAllFiles(output, "__DATASOURCE_PASSWORD__", config.getPassword());` (around line 148). Insert immediately after the `__DATASOURCE_PASSWORD__` line:

```java
        replaceInAllFiles(output, "__S3_ENDPOINT__", config.getS3Endpoint());
        replaceInAllFiles(output, "__S3_REGION__", config.getS3Region());
        replaceInAllFiles(output, "__S3_BUCKET__", config.getS3Bucket());
        replaceInAllFiles(output, "__S3_ACCESS_KEY__", config.getS3AccessKey());
        replaceInAllFiles(output, "__S3_SECRET_KEY__", config.getS3SecretKey());
        replaceInAllFiles(output, "__FILE_DOWNLOAD_TOKEN_SECRET__", config.getFileDownloadTokenSecret());
```

- [ ] **Step 2: Compile app-generator**

Run: `mvn -q -pl app-generator -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java
git commit -m "feat: replace S3 and download token placeholders in backend generation

1. Substitute __S3_ENDPOINT__/__S3_REGION__/__S3_BUCKET__/__S3_ACCESS_KEY__/__S3_SECRET_KEY__/__FILE_DOWNLOAD_TOKEN_SECRET__ during backend skeleton generation"
```

---

### Task 5: Create FileCategoryEnum

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/enums/FileCategoryEnum.java`

- [ ] **Step 1: Create the enum**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/enums/FileCategoryEnum.java`:

```java
package __NAMESPACE__.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import __NAMESPACE__.common.BaseEnum;

/**
 * 文件类别
 *
 * @author Deolin 2026-07-02
 */
@Getter
@AllArgsConstructor
public enum FileCategoryEnum implements BaseEnum<String> {

    IMAGE("image", "图片",
            new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg"))),

    DOCUMENT("document", "文档",
            new HashSet<>(Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md"))),

    ARCHIVE("archive", "压缩包",
            new HashSet<>(Arrays.asList("zip", "rar", "7z", "tar", "gz"))),

    AUDIO("audio", "音频",
            new HashSet<>(Arrays.asList("mp3", "wav", "flac", "aac", "ogg"))),

    VIDEO("video", "视频",
            new HashSet<>(Arrays.asList("mp4", "avi", "mov", "mkv", "webm"))),

    GENERAL("general", "普通文件", null),

    ;

    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            "exe", "bat", "cmd", "sh", "js", "jar", "msi", "com", "scr", "vbs", "dll", "app"));

    private final String code;

    private final String title;

    /**
     * 允许的扩展名白名单，general为null（使用反向黑名单）
     */
    private final Set<String> extensions;

    /**
     * 判断扩展名（小写、不含点）是否允许
     */
    public boolean isExtensionAllowed(String ext) {
        if (ext == null) {
            return false;
        }
        String lower = ext.toLowerCase();
        if (this == GENERAL) {
            return !DANGEROUS_EXTENSIONS.contains(lower);
        }
        return extensions != null && extensions.contains(lower);
    }

    public static FileCategoryEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/enums/FileCategoryEnum.java
git commit -m "feat: add FileCategoryEnum to backend skeleton

1. Six file categories with extension whitelists
2. general category uses a reverse blacklist of dangerous extensions
3. isExtensionAllowed for upload validation"
```

---

### Task 6: Create S3Properties + FileProperties

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/S3Properties.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/FileProperties.java`

- [ ] **Step 1: Create S3Properties.java**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/S3Properties.java` (mirror `AuthcProperties` style):

```java
package __NAMESPACE__.property;

import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import __NAMESPACE__.util.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * S3对象存储配置
 *
 * @author Deolin 2026-07-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "__APP_NAME__.s3")
@Slf4j
@Component
public class S3Properties {

    String endpoint;

    String region;

    /**
     * 桶名，留空时降级为本地存储
     */
    String bucket;

    @JsonIgnore
    String accessKey;

    @JsonIgnore
    String secretKey;

    /**
     * 兼容MinIO等，默认true
     */
    Boolean pathStyleAccess = true;

    /**
     * bucket为空时的本地存储目录
     */
    String localDir = "./file-storage";

    @PostConstruct
    public void init() {
        log.info("__APP_NAME__.s3 properties loaded, bucket={}, localDir={}", bucket, localDir);
    }

}
```

- [ ] **Step 2: Create FileProperties.java**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/FileProperties.java`:

```java
package __NAMESPACE__.property;

import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件相关配置
 *
 * @author Deolin 2026-07-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "__APP_NAME__.file")
@Slf4j
@Component
public class FileProperties {

    @JsonIgnore
    String downloadTokenSecret;

    /**
     * 下载令牌有效期（秒）
     */
    Long downloadTokenTtlSeconds = 300L;

    @PostConstruct
    public void init() {
        log.info("__APP_NAME__.file properties loaded, downloadTokenTtlSeconds={}", downloadTokenTtlSeconds);
    }

}
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/S3Properties.java app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/FileProperties.java
git commit -m "feat: add S3Properties and FileProperties to backend skeleton

1. S3Properties binds __APP_NAME__.s3.* (endpoint/region/bucket/keys/pathStyleAccess/localDir)
2. FileProperties binds __APP_NAME__.file.* (downloadTokenSecret/downloadTokenTtlSeconds)"
```

---

### Task 7: Create FileStorage interface + S3/Local impls + S3Config

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/FileStorage.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/S3FileStorage.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/LocalFileStorage.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/S3Config.java`

- [ ] **Step 1: Create FileStorage interface**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/FileStorage.java`:

```java
package __NAMESPACE__.storage;

/**
 * 文件存储抽象，上传/下载代码仅依赖此接口
 *
 * @author Deolin 2026-07-02
 */
public interface FileStorage {

    /**
     * 存储文件字节
     */
    void store(byte[] bytes, String fileKey);

    /**
     * 读取文件字节
     */
    byte[] load(String fileKey);

    /**
     * 当前存储的bucket标识，本地存储返回null
     */
    String getBucket();

}
```

- [ ] **Step 2: Create S3FileStorage**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/S3FileStorage.java`:

```java
package __NAMESPACE__.storage;

import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import __NAMESPACE__.property.S3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * S3对象存储实现
 *
 * @author Deolin 2026-07-02
 */
@Component
@ConditionalOnProperty(name = "__APP_NAME__.s3.bucket")
@Slf4j
public class S3FileStorage implements FileStorage {

    @Resource
    private S3Client s3Client;

    @Resource
    private S3Properties s3Properties;

    @Override
    public void store(byte[] bytes, String fileKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(fileKey)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    @Override
    public byte[] load(String fileKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(fileKey)
                .build();
        try (ResponseInputStream<?> response = s3Client.getObject(request)) {
            return response.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("读取S3文件失败 fileKey=" + fileKey, e);
        }
    }

    @Override
    public String getBucket() {
        return s3Properties.getBucket();
    }

}
```

- [ ] **Step 3: Create LocalFileStorage**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/LocalFileStorage.java`:

```java
package __NAMESPACE__.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import __NAMESPACE__.property.S3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件存储实现，bucket为空时的兜底
 *
 * @author Deolin 2026-07-02
 */
@Component
@ConditionalOnMissingBean(FileStorage.class)
@Slf4j
public class LocalFileStorage implements FileStorage {

    @Resource
    private S3Properties s3Properties;

    private Path root;

    @PostConstruct
    public void init() throws IOException {
        root = Paths.get(s3Properties.getLocalDir());
        Files.createDirectories(root);
        log.info("LocalFileStorage initialized at {}", root.toAbsolutePath());
    }

    @Override
    public void store(byte[] bytes, String fileKey) {
        try {
            Files.write(root.resolve(fileKey), bytes);
        } catch (IOException e) {
            throw new RuntimeException("写入本地文件失败 fileKey=" + fileKey, e);
        }
    }

    @Override
    public byte[] load(String fileKey) {
        try {
            return Files.readAllBytes(root.resolve(fileKey));
        } catch (IOException e) {
            throw new RuntimeException("读取本地文件失败 fileKey=" + fileKey, e);
        }
    }

    @Override
    public String getBucket() {
        return null;
    }

}
```

- [ ] **Step 4: Create S3Config**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/S3Config.java`:

```java
package __NAMESPACE__.config;

import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import __NAMESPACE__.property.S3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * S3Client配置，仅当bucket非空时构建
 *
 * @author Deolin 2026-07-02
 */
@Configuration
@ConditionalOnProperty(name = "__APP_NAME__.s3.bucket")
@Slf4j
public class S3Config {

    @Resource
    private S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilderExtension builder = S3ClientExtension.builder();
        if (s3Properties.getPathStyleAccess() != null && s3Properties.getPathStyleAccess()) {
            builder.pathStyleAccess(true);
        }
        if (s3Properties.getEndpoint() != null && !s3Properties.getEndpoint().isEmpty()) {
            builder.endpointOverride(s3Properties.getEndpoint());
        }
        if (s3Properties.getRegion() != null && !s3Properties.getRegion().isEmpty()) {
            builder.region(Region.of(s3Properties.getRegion()));
        } else {
            builder.region(Region.US_EAST_1);
        }
        builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())));
        log.info("S3Client built, endpoint={}, bucket={}", s3Properties.getEndpoint(), s3Properties.getBucket());
        return builder.build();
    }

}
```

NOTE: The two helper types `S3ClientBuilderExtension`/`S3ClientExtension` above are placeholders for the real AWS SDK v2 builder API. Before finalizing, read the actual `S3Client.builder()` signature (it returns `S3ClientBuilder`). Replace the body with the real API:

```java
    @Bean
    public S3Client s3Client() {
        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder();
        if (Boolean.TRUE.equals(s3Properties.getPathStyleAccess())) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        if (s3Properties.getEndpoint() != null && !s3Properties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }
        builder.region(Region.of(s3Properties.getRegion() == null || s3Properties.getRegion().isEmpty()
                ? "us-east-1" : s3Properties.getRegion()));
        builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())));
        return builder.build();
    }
```

Use the real API form (second snippet). Add `import java.net.URI;`. Drop the fake helper types entirely — they were only to illustrate intent. The implementer MUST use the second, real snippet.

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/storage/ app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/S3Config.java
git commit -m "feat: add FileStorage abstraction with S3 and local impls

1. FileStorage interface decouples upload/download from storage backend
2. S3FileStorage active when bucket configured
3. LocalFileStorage fallback when bucket empty
4. S3Config builds S3Client bean with path-style access and endpoint override"
```

---

### Task 8: Create DownloadTokenUtils

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/DownloadTokenUtils.java`

- [ ] **Step 1: Create the utility**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/DownloadTokenUtils.java`:

```java
package __NAMESPACE__.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.ErrorCode;
import lombok.experimental.UtilityClass;

/**
 * 无状态HMAC-SHA256签名下载令牌工具
 *
 * token = base64url(payload) + "." + base64url(HMAC-SHA256(secret, payload))
 * payload = fileKey + "|" + expireAtEpochMilli
 *
 * @author Deolin 2026-07-02
 */
@UtilityClass
public class DownloadTokenUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public static String sign(String fileKey, String secret, long ttlSeconds) {
        long expireAt = System.currentTimeMillis() + ttlSeconds * 1000L;
        String payload = fileKey + "|" + expireAt;
        String signature = hmac(secret, payload);
        return base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8)) + "." + signature;
    }

    public static String verify(String token, String secret) {
        if (token == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌为空");
        }
        int dot = token.lastIndexOf('.');
        if (dot < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String payloadPart = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(payloadPart), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String expected = hmac(secret, payload);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌签名校验失败");
        }
        int bar = payload.lastIndexOf('|');
        if (bar < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        String fileKey = payload.substring(0, bar);
        long expireAt;
        try {
            expireAt = Long.parseLong(payload.substring(bar + 1));
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌格式非法");
        }
        if (System.currentTimeMillis() > expireAt) {
            throw new BizException(ErrorCode.BAD_REQUEST, "下载令牌已过期");
        }
        return fileKey;
    }

    private static String hmac(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(raw);
        } catch (Exception e) {
            throw new RuntimeException("HMAC计算失败", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
```

NOTE: `@UtilityClass` (Lombok) makes the class final, gives private constructor, and makes methods static implicitly — but since the methods above are declared `static`, Lombok's `@UtilityClass` would complain about explicit `static`. To be safe and match the skeleton's existing util style, read `UuidUtils.java`/`SecretKeyUtils.java` first: if they use `@UtilityClass`, remove the `static` keywords from the methods above (Lombok adds them); if they use a private constructor throwing `UnsupportedOperationException`, add that constructor and keep `static`. Match whichever pattern the existing utils use. Confirm `BizException` has a `(ErrorCode, String)` constructor (the exploration confirmed it does).

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/DownloadTokenUtils.java
git commit -m "feat: add DownloadTokenUtils for stateless HMAC download tokens

1. sign(fileKey, secret, ttlSeconds) issues base64url(payload).base64url(hmac)
2. verify(token, secret) splits, recomputes HMAC with constant-time compare, checks expiry
3. Multi-node friendly: no shared token store"
```

---

### Task 9: Create response DTOs

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/UploadFileResp.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/TemporarilyDownloadFileResp.java`

- [ ] **Step 1: Create UploadFileResp**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/UploadFileResp.java`:

```java
package __NAMESPACE__.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

/**
 * @author Deolin 2026-07-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadFileResp {

    String fileKey;

    String originFileName;

}
```

- [ ] **Step 2: Create TemporarilyDownloadFileResp**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/TemporarilyDownloadFileResp.java`:

```java
package __NAMESPACE__.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

/**
 * @author Deolin 2026-07-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemporarilyDownloadFileResp {

    String token;

}
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/UploadFileResp.java app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/TemporarilyDownloadFileResp.java
git commit -m "feat: add file upload and download-token response DTOs"
```

---

### Task 10: Create FileService + FileServiceImpl

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/FileService.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/FileServiceImpl.java`

- [ ] **Step 1: Create FileService interface**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/FileService.java`:

```java
package __NAMESPACE__.service;

import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.dto.resp.UploadFileResp;

/**
 * @author Deolin 2026-07-02
 */
public interface FileService {

    UploadFileResp upload(MultipartFile file, String category);

    String temporarilyDownload(String fileKey);

    void download(String token, HttpServletResponse response);

}
```

- [ ] **Step 2: Create FileServiceImpl**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/FileServiceImpl.java`:

```java
package __NAMESPACE__.service.impl;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.common.ErrorCode;
import __NAMESPACE__.dto.resp.UploadFileResp;
import __NAMESPACE__.entity.FileRecordEntity;
import __NAMESPACE__.enums.FileCategoryEnum;
import __NAMESPACE__.mapper.FileRecordMapper;
import __NAMESPACE__.property.FileProperties;
import __NAMESPACE__.service.FileService;
import __NAMESPACE__.storage.FileStorage;
import __NAMESPACE__.util.DownloadTokenUtils;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Deolin 2026-07-02
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStorage fileStorage;

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public UploadFileResp upload(MultipartFile file, String category) {
        FileCategoryEnum categoryEnum = FileCategoryEnum.of(category);
        if (categoryEnum == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件类别非法");
        }
        String originFileName = file.getOriginalFilename();
        String ext = extractExtension(originFileName);
        if (!categoryEnum.isExtensionAllowed(ext)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件扩展名不被允许");
        }
        String fileKey = UuidUtils.generateShort() + (ext != null ? "." + ext : "");
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        try {
            fileStorage.store(file.getBytes(), fileKey);
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }
        FileRecordEntity entity = new FileRecordEntity()
                .setFileKey(fileKey)
                .setOriginFileName(originFileName)
                .setContentType(contentType)
                .setFileSize(file.getSize())
                .setCategory(category)
                .setBucket(fileStorage.getBucket())
                .setCreatedAt(LocalDateTime.now())
                .setCreatedBy(CurrentUser.getUsernameOrDefault("system"));
        fileRecordMapper.insert(entity);
        log.info("file uploaded, fileKey={}, originFileName={}, category={}", fileKey, originFileName, category);
        return new UploadFileResp().setFileKey(fileKey).setOriginFileName(originFileName);
    }

    @Override
    public String temporarilyDownload(String fileKey) {
        FileRecordEntity entity = fileRecordMapper.queryByFileKey(fileKey);
        if (entity == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件不存在");
        }
        return DownloadTokenUtils.sign(fileKey, fileProperties.getDownloadTokenSecret(),
                fileProperties.getDownloadTokenTtlSeconds());
    }

    @Override
    public void download(String token, HttpServletResponse response) {
        String fileKey = DownloadTokenUtils.verify(token, fileProperties.getDownloadTokenSecret());
        FileRecordEntity entity = fileRecordMapper.queryByFileKey(fileKey);
        if (entity == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件不存在");
        }
        byte[] bytes = fileStorage.load(fileKey);
        response.setContentType(entity.getContentType());
        try {
            String filename = URLEncoder.encode(entity.getOriginFileName(), "UTF-8").replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + filename);
            response.setContentLength(bytes.length);
            OutputStream out = response.getOutputStream();
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件下载失败");
        }
    }

    private static String extractExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

}
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/FileService.java app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/FileServiceImpl.java
git commit -m "feat: add FileService upload and download logic

1. upload validates category, extension, stores bytes, inserts file_record
2. temporarilyDownload verifies file exists and signs an HMAC token
3. download verifies token, streams bytes with inline Content-Disposition"
```

---

### Task 11: Create FileController

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/FileController.java`

- [ ] **Step 1: Create the controller**

Create `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/FileController.java`:

```java
package __NAMESPACE__.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.resp.TemporarilyDownloadFileResp;
import __NAMESPACE__.dto.resp.UploadFileResp;
import __NAMESPACE__.service.FileService;

/**
 * 通用文件上传/下载接口
 *
 * @author Deolin 2026-07-02
 */
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("uploadFile")
    public RequestResult<UploadFileResp> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {
        return RequestResult.success(fileService.upload(file, category));
    }

    @PostMapping("temporarilyDownloadFile")
    public RequestResult<TemporarilyDownloadFileResp> temporarilyDownloadFile(
            @RequestParam("fileKey") String fileKey) {
        String token = fileService.temporarilyDownload(fileKey);
        return RequestResult.success(new TemporarilyDownloadFileResp().setToken(token));
    }

    @GetMapping("downloadFile")
    public void downloadFile(@RequestParam("token") String token, HttpServletResponse response) {
        fileService.download(token, response);
    }

}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/FileController.java
git commit -m "feat: add FileController with upload and download endpoints

1. POST uploadFile (multipart, category) - authenticated
2. POST temporarilyDownloadFile (fileKey) - authenticated, signs token
3. GET downloadFile (token) - anonymous, streams file for inline preview"
```

---

### Task 12: Update application.yml

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml`

- [ ] **Step 1: Add multipart + s3 + file config and whitelist downloadFile**

In `app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml`:

1. Under the existing `spring:` block, add `servlet.multipart` limits (place after `spring.application.name` or wherever the `spring:` keys are, keeping valid YAML):

```yaml
spring:
  application:
    name: __APP_NAME__
  servlet:
    multipart:
      max-file-size: 200MB
      max-request-size: 200MB
  datasource:
    # ... existing ...
```

If a `spring:` block already exists, merge `servlet:` into it (do not create a second `spring:` key).

2. Append `/api/v1/file/downloadFile` to the existing `anonymousApiPaths` value. Change:
```yaml
    anonymousApiPaths: /api/v1/authc/login
```
to:
```yaml
    anonymousApiPaths: /api/v1/authc/login,/api/v1/file/downloadFile
```

3. Add the `s3` and `file` blocks under the `__APP_NAME__:` key (after the `authc:` block):

```yaml
  s3:
    endpoint: __S3_ENDPOINT__
    region: __S3_REGION__
    bucket: __S3_BUCKET__
    accessKey: __S3_ACCESS_KEY__
    secretKey: __S3_SECRET_KEY__
    pathStyleAccess: true
    localDir: ./file-storage
  file:
    downloadTokenSecret: __FILE_DOWNLOAD_TOKEN_SECRET__
    downloadTokenTtlSeconds: 300
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml
git commit -m "feat: configure S3, file download, and multipart limits in skeleton yml

1. Add spring.servlet.multipart 200MB limits
2. Whitelist /api/v1/file/downloadFile as anonymous
3. Add __APP_NAME__.s3.* and __APP_NAME__.file.* placeholder blocks"
```

---

### Task 13: Backend build verification

- [ ] **Step 1: Run the full verify**

Run: `mvn -q verify`
Expected: BUILD SUCCESS. If an existing IT fails for reasons unrelated to file component, note it; do not introduce regressions.

- [ ] **Step 2: If build fails, fix and re-run**

Common issues to expect: missing `java.net.URI` import in S3Config; `@UtilityClass` vs explicit static mismatch in DownloadTokenUtils; YAML indentation. Fix each and re-run `mvn -q verify`.

---

## Phase 2: form-generator file itemType

### Task 14: Add FILE to ItemType enum

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ItemType.java`

- [ ] **Step 1: Add the FILE constant**

In `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ItemType.java`, add before the trailing semicolon (after the `TIME("time")` entry):

```java
    /**
     * 文件类字段定义，业务表合并为单列 VARCHAR(512)，值为 fileKey/originFileName
     */
    FILE("file"),
```

- [ ] **Step 2: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ItemType.java
git commit -m "feat: add FILE item type

1. New 'file' enum value for file field type"
```

---

### Task 15: Create FileItemDef

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/item/FileItemDef.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`

- [ ] **Step 1: Create FileItemDef**

Create `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/item/FileItemDef.java` (mirror `SecretItemDef`):

```java
package com.spldeolin.allison1875.formgenerator.dsl.item;

import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * 文件类字段定义
 *
 * @author Deolin 2026-07-02
 */
@Getter
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    @Builder.Default
    ItemType type = ItemType.FILE;

    /**
     * 文件类别，对应后端FileCategoryEnum，默认general
     */
    @Builder.Default
    String category = "general";

    /**
     * 前端UX校验用的最大文件大小（MB），留空不限制
     */
    Integer maxFileSize;

    /**
     * 校验category合法
     */
    @Override
    public void validate() {
        super.validate();
        if (category == null) {
            category = "general";
        }
    }

}
```

NOTE: Read `ItemDef.java` first to confirm whether it has a `validate()` method that subclasses override (and whether `super.validate()` is valid). If `ItemDef` has no `validate()`, remove the override and instead rely on a separate validation mechanism — check how `SelectItemDef`/`TimeItemDef` validate their fields. If there is a `validate()` method, confirm its visibility and that `super.validate()` exists. Match the existing pattern exactly.

- [ ] **Step 2: Register FileItemDef in @JsonSubTypes**

In `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`:

Add the import:
```java
import com.spldeolin.allison1875.formgenerator.dsl.item.FileItemDef;
```

Add a new entry to the `@JsonSubTypes` array (after the `time` entry):
```java
        @JsonSubTypes.Type(value = FileItemDef.class, name = "file")})
```

(i.e., change the closing `]})` of the existing `time` entry's array into `,` + the new line above.)

- [ ] **Step 3: Compile form-generator**

Run: `mvn -q -pl form-generator -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/item/FileItemDef.java form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java
git commit -m "feat: add FileItemDef DSL model

1. FileItemDef carries category (default general) and maxFileSize (nullable)
2. Register file subtype in ItemDef @JsonSubTypes"
```

---

### Task 16: Create FileItemService + wire into PrimaryItemServiceImpl

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FileItemService.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java`

- [ ] **Step 1: Create FileItemService**

Create `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FileItemService.java` (mirror `SecretItemService`, but not filterable, not sortable, VARCHAR(512)):

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.FileItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-07-02
 */
@Singleton
public class FileItemService implements ItemService<FileItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.FILE;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(FileItemDef itemDef) {
        return null;
    }

    @Override
    public Boolean isSortable(FileItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(FileItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(FileItemDef itemDef) {
        return "VARCHAR(512)";
    }

    @Override
    public String getJavaTypeInDTO(FileItemDef itemDef) {
        return "String";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(FileItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notEmpty());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(FileItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(FileItemDef itemDef) {
        return "\"\"";
    }

    @Override
    public Statement getValidationStatement(FileItemDef itemDef) {
        return parseStatement(
                "if (!StringUtils.hasText(req.get%s())) { throw new IllegalArgumentException(\"%s不能为空\"); }",
                StringUtils.capitalize(itemDef.getName()), itemDef.getTitle());
    }

}
```

NOTE: Confirm `annotationExprService.notEmpty()` exists (used by `SecretItemService`). The exploration confirmed it. Keep the method identical to SecretItemService's.

- [ ] **Step 2: Wire into PrimaryItemServiceImpl**

In `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java`:

Add injection field (near the other `@Inject` fields):
```java
    @Inject
    private FileItemService fileItemService;
```

Add a `case FILE` to the `delegate()` switch (before the closing `}` of the switch):
```java
            case FILE:
                return (ItemService<I>) fileItemService;
```

- [ ] **Step 3: Compile**

Run: `mvn -q -pl form-generator -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FileItemService.java form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/PrimaryItemServiceImpl.java
git commit -m "feat: add FileItemService and wire FILE dispatch

1. FileItemService returns VARCHAR(512) column, String DTO type, not filterable, not sortable
2. PrimaryItemServiceImpl delegates FILE to FileItemService"
```

---

### Task 17: Confirm file excluded from sort enums

**Files:**
- Verify: `form-generator/src/main/java/.../FormGenerator.java` and `.../EnumServiceImpl.java`

- [ ] **Step 1: Verify no FILE entry in sort-allow lists**

The exploration found sort enums are generated only for `NUMBER/ON_OFF/TEXT/TIME` (explicit allow-lists in `FormGenerator.java` ~line 179 and `EnumServiceImpl.java` ~line 127). Since FILE is not in those allow-lists, it is automatically excluded. No code change needed.

Confirm by running:
```bash
grep -rn "ItemType\." form-generator/src/main/java | grep -i "sort\|sortable" 
```
Verify the allow-lists do not mention FILE. If any allow-list uses a deny-list pattern (e.g. "everything except MULTI_SELECT"), then FILE WOULD be wrongly included — in that case add FILE to the exclusion. Report what you find.

- [ ] **Step 2: Commit only if a change was needed**

If no change: skip commit, note "verified, no change needed". If a change was needed, commit with message describing the exclusion.

---

### Task 18: Handle file field in audit log expansion

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/AppGeneratorMutationExpansionServiceImpl.java`

- [ ] **Step 1: Update audit content generation for file fields**

The spec says: file field audit value should be `originFileName` (the part after the first `/` in the merged column), not the raw `fileKey/originFileName` string.

In `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/AppGeneratorMutationExpansionServiceImpl.java`:

In `expandCreateMethodBody()`, the loop currently does:
```java
                if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                    body.addStatement(parseStatement("auditContent.put(\"%s\", req.get%s());",
                            item.getTitle(), StringUtils.capitalize(item.getName())));
                }
```

Change it to branch on file type:
```java
                if (Boolean.TRUE.equals(item.getCanInputOnInit())) {
                    if (item.getType() == ItemType.FILE) {
                        body.addStatement(parseStatement(
                                "auditContent.put(\"%s\", req.get%s() == null ? null : req.get%s().substring(req.get%s().indexOf('/') + 1));",
                                item.getTitle(), StringUtils.capitalize(item.getName()),
                                StringUtils.capitalize(item.getName()), StringUtils.capitalize(item.getName())));
                    } else {
                        body.addStatement(parseStatement("auditContent.put(\"%s\", req.get%s());",
                                item.getTitle(), StringUtils.capitalize(item.getName())));
                    }
                }
```

Apply the analogous change in `expandUpdateMethodBody()` for BOTH the `oldValues.put` and `newValues.put` statements (each gets the same file/non-file branch; for `oldValues` use `form.getVarName().get{Capitalized}()` instead of `req.get{Capitalized}()`).

Confirm `ItemType` is already imported (it is, per the exploration).

- [ ] **Step 2: Compile**

Run: `mvn -q -pl app-generator -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/AppGeneratorMutationExpansionServiceImpl.java
git commit -m "feat: audit file fields by origin file name

1. File field audit content takes the originFileName portion (after first '/') of the merged column
2. Applies to create and update audit content"
```

---

### Task 19: form-generator build verification

- [ ] **Step 1: Run verify**

Run: `mvn -q verify`
Expected: BUILD SUCCESS, no new test failures.

- [ ] **Step 2: Fix any failures**

If an IT that parses a forms.yml now fails because of an unexpected `file` type in a fixture, that's expected only if a fixture uses `file` — none should yet. Investigate any failure.

---

## Phase 3: Frontend file component

### Task 20: Add FileItemDef and FileValue types

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts`

- [ ] **Step 1: Read the current types.ts**

Read `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts` to see the exact `ItemDef` union shape and where to add `FileItemDef`.

- [ ] **Step 2: Add FileItemDef and FileValue**

Add a `FileValue` interface and a `FileItemDef` to the union. Match the style of existing item defs (e.g. `TextItemDef`, `SelectItemDef`). Conceptually:

```typescript
export interface FileValue {
  fileKey: string
  originFileName: string
}

export interface FileItemDef {
  type: 'file'
  name: string
  title: string
  isNonVoid?: boolean
  canInputOnInit?: boolean
  canInputOnEdit?: boolean
  category?: string
  maxFileSize?: number
}
```

Add `FileItemDef` to the `ItemDef` union type (the `ItemDef = TextItemDef | NumberItemDef | ... | FileItemDef` line).

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts
git commit -m "feat: add FileItemDef and FileValue frontend types"
```

---

### Task 21: Create file-category.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/fields/file-category.ts`

- [ ] **Step 1: Create the category mirror**

Create `app-generator/src/main/resources/frontend-skeleton/src/core/fields/file-category.ts`:

```typescript
const EXTENSIONS: Record<string, string[]> = {
  image: ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'],
  document: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv', 'md'],
  archive: ['zip', 'rar', '7z', 'tar', 'gz'],
  audio: ['mp3', 'wav', 'flac', 'aac', 'ogg'],
  video: ['mp4', 'avi', 'mov', 'mkv', 'webm'],
}

const DANGEROUS_EXTENSIONS = new Set([
  'exe', 'bat', 'cmd', 'sh', 'js', 'jar', 'msi', 'com', 'scr', 'vbs', 'dll', 'app',
])

const CATEGORY_TITLES: Record<string, string> = {
  image: '图片',
  document: '文档',
  archive: '压缩包',
  audio: '音频',
  video: '视频',
  general: '普通文件',
}

export function acceptOf(category: string | undefined): string {
  const cat = category || 'general'
  if (cat === 'general') {
    return ''
  }
  const exts = EXTENSIONS[cat]
  if (!exts) {
    return ''
  }
  return exts.map((e) => '.' + e).join(',')
}

export function isExtensionAllowed(category: string | undefined, fileName: string): boolean {
  const cat = category || 'general'
  const ext = extractExt(fileName)
  if (!ext) {
    return false
  }
  if (cat === 'general') {
    return !DANGEROUS_EXTENSIONS.has(ext)
  }
  const exts = EXTENSIONS[cat]
  return !!exts && exts.includes(ext)
}

export function hintOf(category: string | undefined, maxFileSize?: number): string {
  const cat = category || 'general'
  const title = CATEGORY_TITLES[cat] || '普通文件'
  const parts: string[] = [`类别：${title}`]
  if (cat !== 'general') {
    parts.push(`支持：${(EXTENSIONS[cat] || []).join('、')}`)
  }
  if (maxFileSize) {
    parts.push(`大小上限：${maxFileSize}MB`)
  }
  return parts.join('；')
}

export function extractExt(fileName: string | undefined): string | null {
  if (!fileName) {
    return null
  }
  const dot = fileName.lastIndexOf('.')
  if (dot < 0 || dot === fileName.length - 1) {
    return null
  }
  return fileName.substring(dot + 1).toLowerCase()
}

export function isPreviewableImage(fileName: string | undefined): boolean {
  const ext = extractExt(fileName)
  return !!ext && ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
}

export function isPreviewablePdf(fileName: string | undefined): boolean {
  return extractExt(fileName) === 'pdf'
}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/fields/file-category.ts
git commit -m "feat: add file-category frontend mirror

1. acceptOf/hintOf/isExtensionAllowed mirror backend FileCategoryEnum
2. isPreviewableImage/isPreviewablePdf for inline preview branching"
```

---

### Task 22: Create file-api.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/file-api.ts`

- [ ] **Step 1: Read the existing request util**

Read `app-generator/src/main/resources/frontend-skeleton/src/utils/request.ts` to see the HTTP helper signature (how auth header is attached, how the base URL is built, what the return shape is). Match its style.

- [ ] **Step 2: Create file-api.ts**

Create `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/file-api.ts`:

```typescript
import { request } from '@/utils/request'
import type { FileValue } from '@/schema/types'

export interface UploadFileResp {
  fileKey: string
  originFileName: string
}

export async function uploadFile(file: File, category: string): Promise<FileValue> {
  const form = new FormData()
  form.append('file', file)
  form.append('category', category)
  const resp = await request<UploadFileResp>('/api/v1/file/uploadFile', {
    method: 'POST',
    body: form,
  })
  return { fileKey: resp.fileKey, originFileName: resp.originFileName }
}

export async function fetchDownloadToken(fileKey: string): Promise<string> {
  const resp = await request<{ token: string }>('/api/v1/file/temporarilyDownloadFile', {
    method: 'POST',
    params: { fileKey },
  })
  return resp.token
}

export function downloadUrlOf(token: string): string {
  return '/api/v1/file/downloadFile?token=' + encodeURIComponent(token)
}
```

NOTE: Adapt the `request<T>(url, opts)` signature to whatever `utils/request.ts` actually exports. If it uses `axios` instead of fetch, rewrite using `axios.post(url, form, { headers: { 'Content-Type': 'multipart/form-data' } })`. Read the file first and match exactly — do not invent a request API that doesn't exist.

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/file-api.ts
git commit -m "feat: add file-api frontend client

1. uploadFile posts multipart to /api/v1/file/uploadFile
2. fetchDownloadToken posts fileKey to /api/v1/file/temporarilyDownloadFile
3. downloadUrlOf builds the anonymous download URL"
```

---

### Task 23: Create FileField.vue (four-state)

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/fields/FileField.vue`

- [ ] **Step 1: Read an existing field component and the visual mockup**

Read `app-generator/src/main/resources/frontend-skeleton/src/core/fields/TextField.vue` and `docs/file-component/approved-visual-mockup.html` to ground the component structure (props, emits, mode handling).

- [ ] **Step 2: Create FileField.vue**

Create `app-generator/src/main/resources/frontend-skeleton/src/core/fields/FileField.vue`:

```vue
<template>
  <!-- display mode (list/detail) -->
  <span v-if="mode === 'display'" class="file-field-display">
    <template v-if="value">
      <NIcon size="16"><DocumentIcon /></NIcon>
      <span class="file-name" :title="value.originFileName">{{ value.originFileName }}</span>
      <NButton text @click="openPreview">
        <template #icon><NIcon><EyeIcon /></NIcon></template>
      </NButton>
    </template>
    <span v-else>-</span>
  </span>

  <!-- edit mode -->
  <div v-else class="file-field-edit">
    <!-- not uploaded yet -->
    <NUploadDragger
      v-if="!value"
      :accept="accept"
      :show-file-list="false"
      :custom-request="handleUpload"
    >
      <div class="upload-hint">{{ hint }}</div>
    </NUploadDragger>

    <!-- uploaded -->
    <div v-else class="file-card">
      <NIcon size="20"><DocumentIcon /></NIcon>
      <span class="file-name" :title="value.originFileName">{{ value.originFileName }}</span>
      <NButton text @click="openPreview">
        <template #icon><NIcon><EyeIcon /></NIcon></template>
      </NButton>
      <NButton text @click="removeFile" :disabled="!editable">
        <template #icon><NIcon><TrashIcon /></NIcon></template>
      </NButton>
    </div>
  </div>

  <!-- preview modal -->
  <NModal v-model:show="previewVisible" preset="card" :title="value?.originFileName" style="width: 720px">
    <div v-if="previewLoading" class="preview-loading">
      <NSpin />
    </div>
    <div v-else-if="previewToken" class="preview-content">
      <NImage v-if="isImage" :src="downloadUrl" object-fit="contain" style="max-width: 100%" />
      <iframe v-else-if="isPdf" :src="downloadUrl" style="width: 100%; height: 70vh; border: none" />
      <div v-else class="preview-fallback">
        <NButton tag="a" :href="downloadUrl" target="_blank">下载</NButton>
      </div>
    </div>
  </NModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  NUploadDragger,
  NButton,
  NIcon,
  NModal,
  NImage,
  NSpin,
  useMessage,
} from 'naive-ui'
import type { UploadCustomRequestOptions } from 'naive-ui'
import type { FileItemDef, FileValue } from '@/schema/types'
import {
  acceptOf,
  hintOf,
  isExtensionAllowed,
  isPreviewableImage,
  isPreviewablePdf,
} from './file-category'
import { uploadFile, fetchDownloadToken, downloadUrlOf } from '@/core/protocol/file-api'

const props = defineProps<{
  item: FileItemDef
  value: string | FileValue | null
  mode: 'display' | 'edit'
  editable?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:value', v: string | FileValue | null): void
}>()

const message = useMessage()

const accept = computed(() => acceptOf(props.item.category))
const hint = computed(() => hintOf(props.item.category, props.item.maxFileSize))

const fileValue = computed<FileValue | null>(() => {
  if (!props.value) return null
  if (typeof props.value === 'string') {
    const bar = props.value.indexOf('/')
    if (bar < 0) return null
    return {
      fileKey: props.value.substring(0, bar),
      originFileName: props.value.substring(bar + 1),
    }
  }
  return props.value
})

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewToken = ref('')
const isImage = computed(() => isPreviewableImage(fileValue.value?.originFileName))
const isPdf = computed(() => isPreviewablePdf(fileValue.value?.originFileName))
const downloadUrl = computed(() => previewToken.value ? downloadUrlOf(previewToken.value) : '')

async function handleUpload({ file }: UploadCustomRequestOptions) {
  const raw = file.file
  if (!raw) return
  if (!isExtensionAllowed(props.item.category, raw.name)) {
    message.error('文件扩展名不被允许')
    return
  }
  if (props.item.maxFileSize && raw.size > props.item.maxFileSize * 1024 * 1024) {
    message.error(`文件大小超过 ${props.item.maxFileSize}MB`)
    return
  }
  try {
    const result = await uploadFile(raw, props.item.category || 'general')
    emit('update:value', result)
    message.success('上传成功')
  } catch (e) {
    message.error('上传失败')
  }
}

function removeFile() {
  emit('update:value', null)
}

async function openPreview() {
  if (!fileValue.value) return
  previewVisible.value = true
  previewLoading.value = true
  try {
    previewToken.value = await fetchDownloadToken(fileValue.value.fileKey)
  } catch (e) {
    message.error('获取下载令牌失败')
    previewVisible.value = false
  } finally {
    previewLoading.value = false
  }
}
</script>

<style scoped>
.file-field-display {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.file-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-field-edit .file-card {
  display: flex;
  align-items: center;
  gap: 8px;
}
.upload-hint {
  padding: 16px;
  color: #909399;
  font-size: 13px;
}
.preview-loading,
.preview-fallback {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}
</style>
```

NOTE: This uses placeholder icons `DocumentIcon`/`EyeIcon`/`TrashIcon`. naive-ui does not bundle these as named exports by default — replace with naive-ui's `@vicons/ionicons5` (or whichever icon library the skeleton already uses; check `package.json` and existing components). Read an existing component to see how icons are imported and match that. Do not leave undefined icon components — either import real icons or remove the `<NIcon>` wrappers and use plain text/emoji as a fallback during iteration. Confirm `editable` prop semantics against how `TextField.vue` handles edit-vs-display; the mockup is roughly approved so visual polish can iterate.

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/fields/FileField.vue
git commit -m "feat: add FileField four-state component

1. Display state: compact cell with filename and preview eye
2. Edit not-uploaded: NUploadDragger with category hint
3. Edit uploaded: file card with preview/remove
4. Preview modal: image via NImage, PDF via iframe, else download link
5. Two-step upload: validate accept/size, then uploadFile stores FileValue"
```

---

### Task 24: Wire FileField into FieldRenderer

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue`

- [ ] **Step 1: Read FieldRenderer.vue**

Read `app-generator/src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue` to see how it maps `item.type` to components.

- [ ] **Step 2: Register file**

Add the import and the mapping entry for `file` → `FileField`, matching the existing pattern (likely a `components` map or a `<component :is>` lookup). Example if it uses a map:

```typescript
import FileField from './FileField.vue'
// in the type→component map:
  file: FileField,
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue
git commit -m "feat: register FileField in FieldRenderer"
```

---

### Task 25: Wire request-builder file join/split

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts`

- [ ] **Step 1: Read request-builder.ts**

Read `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts` — focus on `buildCreateRequest`, `buildUpdateRequest`, and how detail/list responses are parsed back into form state.

- [ ] **Step 2: Join FileValue on submit, split on load**

On submit (create/update): when a field's type is `file`, convert the `FileValue` to the merged string `fileKey + '/' + originFileName` before placing it in the request body. On load (detail/list response): when a field's type is `file`, split the string on the FIRST `/` back into `FileValue`.

Find where field values are read for the request body and add:

```typescript
function serializeFileValue(v: FileValue | string | null): string | null {
  if (!v) return null
  if (typeof v === 'string') return v
  return v.fileKey + '/' + v.originFileName
}
```

Call `serializeFileValue` for file-typed fields when building create/update requests. For parsing responses back, find the detail-load path and split:

```typescript
function parseFileValue(s: string | null): FileValue | null {
  if (!s) return null
  const bar = s.indexOf('/')
  if (bar < 0) return null
  return { fileKey: s.substring(0, bar), originFileName: s.substring(bar + 1) }
}
```

Apply `parseFileValue` to file-typed fields when hydrating form state from a detail/list response (if request-builder is responsible for that; otherwise apply wherever form state is hydrated — check EditModal.vue / useCrudPage.ts).

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts
git commit -m "feat: join/split file field as merged single column

1. Serialize FileValue to 'fileKey/originFileName' on submit
2. Split merged column on first '/' when hydrating form state"
```

---

### Task 26: Mark file not searchable in field-policy

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts`

- [ ] **Step 1: Read field-policy.ts**

Read `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts` — find `FILTER_PATTERNS_BY_TYPE` and `isVisible`/`isEditable`.

- [ ] **Step 2: Exclude file from search**

Add `file: []` to `FILTER_PATTERNS_BY_TYPE` (no filter patterns — same as `secret`). If `isVisible` decides search-bar presence by whether `FILTER_PATTERNS_BY_TYPE[type]` is non-empty, this already excludes file. Confirm; if search presence is decided elsewhere, ensure file is excluded from the search mode there.

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts
git commit -m "feat: mark file field not searchable

1. file type has no filter patterns, excluding it from the search form"
```

---

## Phase 4: Documentation

### Task 27: Update CLAUDE.md docs and SKILL.md

**Files:**
- Modify: `form-generator/CLAUDE.md`
- Modify: `app-generator/backend-skeleton/CLAUDE.md` (find exact path)
- Modify: `skills/integrate-allison1875/SKILL.md`

- [ ] **Step 1: form-generator/CLAUDE.md**

Add `file` to the itemType list / DSL field reference: document `category` (default general, one of FileCategoryEnum), `maxFileSize` (nullable MB), and the merged single-column `fileKey/originFileName` format (VARCHAR(512), first `/` splits). Note file is not searchable and not in SortEnum.

- [ ] **Step 2: backend-skeleton/CLAUDE.md**

Document the file facilities: `FileStorage` interface + S3/Local impls (bucket empty → local fallback), `FileCategoryEnum` (6 categories, general reverse blacklist), `FileController` endpoints (uploadFile/temporarilyDownloadFile/downloadFile), `DownloadTokenUtils` (stateless HMAC), `file_record` table, `anonymousApiPaths` includes downloadFile, the 6 `__S3_*__`/`__FILE_DOWNLOAD_TOKEN_SECRET__` placeholders.

- [ ] **Step 3: skills/integrate-allison1875/SKILL.md**

Add the 6 new Config fields (s3Endpoint/s3Region/s3Bucket/s3AccessKey/s3SecretKey/fileDownloadTokenSecret) to the Config field reference, with defaults (5 S3 fields default empty, fileDownloadTokenSecret required non-null) and the S3-not-configured → local-storage behavior.

- [ ] **Step 4: Commit**

```bash
git add form-generator/CLAUDE.md app-generator/backend-skeleton/CLAUDE.md skills/integrate-allison1875/SKILL.md
git commit -m "docs: document file component across modules

1. form-generator CLAUDE.md: file itemType DSL and merged column
2. backend-skeleton CLAUDE.md: storage, upload/download, token, placeholders
3. SKILL.md: 6 new S3/token Config fields"
```

---

### Task 28: Final full build + verification

- [ ] **Step 1: Run full verify**

Run: `mvn -q verify`
Expected: BUILD SUCCESS, all existing ITs pass.

- [ ] **Step 2: Frontend type-check (if a script exists)**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npm run build` (or `vue-tsc --noEmit` if available). Expected: no type errors in the new file component files. If there's no build setup in the skeleton (it's a template), skip and note it.

- [ ] **Step 3: Report**

Report: backend compiles, form-generator compiles + ITs pass, frontend types added. Phase 4 (super DSL integration) is the user's responsibility per the spec.

---

## Self-Review Notes

- **Spec coverage:** Part 1 (deps/config/storage) → Tasks 1,2,4,6,7,12. Part 2 (upload + enum) → Tasks 5,9,10,11. Part 3 (HMAC token + 2 endpoints) → Tasks 8,10,11,12. Part 4 (form-generator file itemType) → Tasks 14,15,16,17,18. Part 5 (frontend) → Tasks 20-26. Docs → Task 27. ConfigValidator enforcement added (Task 3) since spec says fileDownloadTokenSecret "不能为空" but did not name where — ConfigValidator is the natural place.
- **Merge-column invariant:** FileItemService returns VARCHAR(512) + String (Task 16); request-builder joins/splits (Task 25); audit takes originFileName (Task 18). file_record stays two-column (no change — already generated).
- **No special-case branching in form-generator CRUD services:** confirmed by exploration — they route through PrimaryItemServiceImpl. FILE just needs correct ItemService return values (Task 16) + sort exclusion (Task 17, automatic).
- **Placeholders flagged for implementer attention:** S3Config real AWS builder API (Task 7), DownloadTokenUtils util-class style (Task 8), FileItemDef.validate() existence (Task 15), file-api request signature (Task 22), FileField icons (Task 23), request-builder hydrate path (Task 25). Each step instructs reading the real file first.
