# Config Module Immutable Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `Config` and `DomainConfig` immutable after YAML deserialization, replace annotation-based validation with an explicit procedural method, and add unit tests.

**Architecture:** Switch from SnakeYaml + `@Data` + jakarta.validation annotations to Jackson YAMLMapper + Lombok `@Value`/`@Jacksonized`/`@Builder(toBuilder=true)`. A static factory method `Config.fromYaml(File)` handles deserialization, defaults, and validation in one call. FormGenerator and AppGenerator use `toBuilder()` to derive local Config copies and construct new Guice Injectors.

**Tech Stack:** Jackson YAMLMapper (jackson-dataformat-yaml), Lombok @Value/@Jacksonized/@Builder, JUnit 5

---

### Task 1: Add jackson-dataformat-yaml dependency to common module

**Files:**
- Modify: `common/pom.xml`
- Modify: `form-generator/pom.xml`

- [ ] **Step 1: Add jackson-dataformat-yaml to common/pom.xml**

Add after the existing `jackson-datatype-jsr310` dependency:

```xml
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
        </dependency>
```

- [ ] **Step 2: Remove jackson-dataformat-yaml from form-generator/pom.xml**

Remove the following block from `form-generator/pom.xml`:

```xml
        <!-- jackson yaml -->
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
        </dependency>
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl common,form-generator -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add common/pom.xml form-generator/pom.xml
git commit -m "$(cat <<'EOF'
build: move jackson-dataformat-yaml from form-generator to common

1. Add jackson-dataformat-yaml dependency to common module
2. Remove it from form-generator (now inherited transitively)

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Rewrite Config as immutable @Value class with static factory

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java`

- [ ] **Step 1: Rewrite Config.java**

Replace the entire file with:

```java
package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

/**
 * Allison1875 统一配置类，整合所有模块的配置项。
 *
 * @author Deolin 2026-03-12
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class Config {

    // ==================== 公共配置 ====================

    List<DomainConfig> domains;

    String author;

    Boolean isDataModelWithoutLombok;

    Boolean enableNoModifyAnnounce;

    Boolean enableJavaxMoveToJakarta;

    String javaHome;

    // ==================== Guice Module 配置 ====================

    String docAnalyzerModule;

    String handlerTransformerModule;

    String persistenceGeneratorModule;

    String queryTransformerModule;

    String starTransformerModule;

    String formGeneratorModule;

    String appGeneratorModule;

    // ==================== handler-transformer 配置 ====================

    Boolean enableOneService;

    // ==================== persistence-generator 配置 ====================

    String jdbcUrl;

    String userName;

    String password;

    String schema;

    String ddl;

    List<String> tables;

    Boolean enableGenerateDesign;

    Boolean isEntityEndWithEntity;

    String deletedSql;

    String notDeletedSql;

    // ==================== star-transformer 配置 ====================

    String wholeDTONamePostfix;

    // ==================== doc-analyzer 配置 ====================

    List<File> dependencyDirsOrJavaFilePath;

    String globalUrlPrefix;

    List<FlushToEnum> flushTo;

    String yapiUrl;

    String yapiToken;

    File markdownDir;

    String showdocBaseCatName;

    String showdocUrl;

    String showdocApiKey;

    String showdocApiToken;

    File dslDir;

    Boolean singleEndpointPerMarkdown;

    List<String> mvcHandlerQualifierWildcards;

    String getEnumCodeMethodName;

    String getEnumTitleMethodName;

    // ==================== app-generator 配置 ====================

    File appDslPath;

    File appGeneratorOutputDir;

    // ==================== form-generator 配置 ====================

    File dslPath;

    CodeSnippet codeSnippet;

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    public static class CodeSnippet {

        String requestResultQualifier;

        String requestResultTypeDeclaration;

        String requestResultSuccessNoData;

        String requestResultSuccessWithData;

        String controllerRequestMapping;

        String shortUuidGeneration;

        String collectionEmptyCheck;

        String bizExceptionQualifier;

        static CodeSnippet applyDefaults(CodeSnippet raw) {
            if (raw == null) {
                raw = CodeSnippet.builder().build();
            }
            return raw.toBuilder()
                    .controllerRequestMapping(
                            raw.getControllerRequestMapping() != null ? raw.getControllerRequestMapping()
                                    : "/api/v1/${formName}")
                    .shortUuidGeneration(raw.getShortUuidGeneration() != null ? raw.getShortUuidGeneration()
                            : "UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()")
                    .collectionEmptyCheck(raw.getCollectionEmptyCheck() != null ? raw.getCollectionEmptyCheck()
                            : "${list} == null || ${list}.isEmpty()")
                    .bizExceptionQualifier(raw.getBizExceptionQualifier() != null ? raw.getBizExceptionQualifier()
                            : "java.lang.RuntimeException")
                    .build();
        }

    }

    public static Config fromYaml(File yamlFile) {
        Config raw = deserialize(yamlFile);
        Config withDefaults = applyDefaults(raw);
        validate(withDefaults);
        return withDefaults;
    }

    private static Config deserialize(File yamlFile) {
        YAMLMapper mapper = YAMLMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        try {
            return mapper.readValue(yamlFile, Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException("读取配置文件失败: " + yamlFile.getAbsolutePath(), e);
        }
    }

    private static Config applyDefaults(Config raw) {
        List<DomainConfig> domains = raw.getDomains();
        if (domains != null) {
            List<DomainConfig> domainsWithDefaults = new ArrayList<>(domains.size());
            for (DomainConfig dc : domains) {
                domainsWithDefaults.add(DomainConfig.applyDefaults(dc));
            }
            domains = domainsWithDefaults;
        }

        return raw.toBuilder()
                .domains(domains)
                .author(raw.getAuthor() != null ? raw.getAuthor() : "Allison 1875")
                .isDataModelWithoutLombok(
                        raw.getIsDataModelWithoutLombok() != null ? raw.getIsDataModelWithoutLombok() : false)
                .enableNoModifyAnnounce(
                        raw.getEnableNoModifyAnnounce() != null ? raw.getEnableNoModifyAnnounce() : true)
                .enableJavaxMoveToJakarta(
                        raw.getEnableJavaxMoveToJakarta() != null ? raw.getEnableJavaxMoveToJakarta() : false)
                .docAnalyzerModule(raw.getDocAnalyzerModule() != null ? raw.getDocAnalyzerModule()
                        : "com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule")
                .handlerTransformerModule(raw.getHandlerTransformerModule() != null
                        ? raw.getHandlerTransformerModule()
                        : "com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule")
                .persistenceGeneratorModule(raw.getPersistenceGeneratorModule() != null
                        ? raw.getPersistenceGeneratorModule()
                        : "com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule")
                .queryTransformerModule(raw.getQueryTransformerModule() != null ? raw.getQueryTransformerModule()
                        : "com.spldeolin.allison1875.querytransformer.QueryTransformerModule")
                .starTransformerModule(raw.getStarTransformerModule() != null ? raw.getStarTransformerModule()
                        : "com.spldeolin.allison1875.startransformer.StarTransformerModule")
                .formGeneratorModule(raw.getFormGeneratorModule() != null ? raw.getFormGeneratorModule()
                        : "com.spldeolin.allison1875.formgenerator.FormGeneratorModule")
                .appGeneratorModule(raw.getAppGeneratorModule() != null ? raw.getAppGeneratorModule()
                        : "com.spldeolin.allison1875.appgenerator.AppGeneratorModule")
                .enableOneService(raw.getEnableOneService() != null ? raw.getEnableOneService() : false)
                .tables(raw.getTables() != null ? raw.getTables() : new ArrayList<>())
                .enableGenerateDesign(raw.getEnableGenerateDesign() != null ? raw.getEnableGenerateDesign() : true)
                .isEntityEndWithEntity(
                        raw.getIsEntityEndWithEntity() != null ? raw.getIsEntityEndWithEntity() : true)
                .wholeDTONamePostfix(
                        raw.getWholeDTONamePostfix() != null ? raw.getWholeDTONamePostfix() : "WholeDTO")
                .dependencyDirsOrJavaFilePath(raw.getDependencyDirsOrJavaFilePath() != null
                        ? raw.getDependencyDirsOrJavaFilePath() : new ArrayList<>())
                .globalUrlPrefix(raw.getGlobalUrlPrefix() != null ? raw.getGlobalUrlPrefix() : "")
                .flushTo(raw.getFlushTo() != null ? raw.getFlushTo() : List.of(FlushToEnum.MARKDOWN))
                .markdownDir(raw.getMarkdownDir() != null ? raw.getMarkdownDir() : new File("api-docs"))
                .showdocBaseCatName(
                        raw.getShowdocBaseCatName() != null ? raw.getShowdocBaseCatName() : "doc-analyzer")
                .dslDir(raw.getDslDir() != null ? raw.getDslDir() : new File("api-dsls"))
                .singleEndpointPerMarkdown(
                        raw.getSingleEndpointPerMarkdown() != null ? raw.getSingleEndpointPerMarkdown() : false)
                .getEnumCodeMethodName(
                        raw.getGetEnumCodeMethodName() != null ? raw.getGetEnumCodeMethodName() : "getCode")
                .getEnumTitleMethodName(
                        raw.getGetEnumTitleMethodName() != null ? raw.getGetEnumTitleMethodName() : "getTitle")
                .appDslPath(raw.getAppDslPath() != null ? raw.getAppDslPath() : new File("./app.yml"))
                .appGeneratorOutputDir(
                        raw.getAppGeneratorOutputDir() != null ? raw.getAppGeneratorOutputDir() : new File("./output"))
                .dslPath(raw.getDslPath() != null ? raw.getDslPath() : new File("./forms.yml"))
                .codeSnippet(CodeSnippet.applyDefaults(raw.getCodeSnippet()))
                .build();
    }

    private static void validate(Config config) {
        List<String> errors = new ArrayList<>();

        // domains
        if (config.getDomains() == null || config.getDomains().isEmpty()) {
            errors.add("domains must not be empty");
        } else {
            for (int i = 0; i < config.getDomains().size(); i++) {
                DomainConfig.validate(config.getDomains().get(i), "domains[" + i + "]", errors);
            }
        }

        // persistence-generator 条件校验
        if (config.getJdbcUrl() != null || config.getDdl() != null) {
            if (StringUtils.isAllEmpty(config.getJdbcUrl(), config.getDdl())) {
                errors.add("jdbcUrl and ddl must not both be null");
            } else if (StringUtils.isNotEmpty(config.getJdbcUrl())) {
                if (StringUtils.isEmpty(config.getUserName())) {
                    errors.add("userName must not be empty when jdbcUrl is not empty");
                }
                if (StringUtils.isEmpty(config.getPassword())) {
                    errors.add("password must not be empty when jdbcUrl is not empty");
                }
                if (StringUtils.isEmpty(config.getSchema())) {
                    errors.add("schema must not be empty when jdbcUrl is not empty");
                }
            }
        }

        // doc-analyzer flushTo 条件校验
        if (config.getFlushTo() != null) {
            if (config.getFlushTo().contains(FlushToEnum.YAPI)) {
                if (config.getYapiUrl() == null) {
                    errors.add("yapiUrl must not be null when flushTo contains 'YAPI'");
                }
                if (config.getYapiToken() == null) {
                    errors.add("yapiToken must not be null when flushTo contains 'YAPI'");
                }
            }
            if (config.getFlushTo().contains(FlushToEnum.MARKDOWN)) {
                if (config.getMarkdownDir() == null) {
                    errors.add("markdownDirectoryPath must not be null when flushTo contains 'MARKDOWN'");
                }
            }
            if (config.getFlushTo().contains(FlushToEnum.DSL)) {
                if (config.getDslDir() == null) {
                    errors.add("dslDir must not be null when flushTo contains 'DSL'");
                }
            }
            if (config.getFlushTo().contains(FlushToEnum.SHOWDOC)) {
                if (config.getShowdocUrl() == null) {
                    errors.add("showdocUrl must not be null when flushTo contains 'SHOWDOC'");
                }
                if (config.getShowdocApiKey() == null) {
                    errors.add("showdocApiKey must not be null when flushTo contains 'SHOWDOC'");
                }
                if (config.getShowdocApiToken() == null) {
                    errors.add("showdocApiToken must not be null when flushTo contains 'SHOWDOC'");
                }
            }
        }

        // codeSnippet 互相依赖校验
        if (config.getCodeSnippet() != null) {
            CodeSnippet cs = config.getCodeSnippet();
            boolean hasQualifier = StringUtils.isNotEmpty(cs.getRequestResultQualifier());
            boolean hasTypeDecl = StringUtils.isNotEmpty(cs.getRequestResultTypeDeclaration());
            boolean hasSuccessNoData = StringUtils.isNotEmpty(cs.getRequestResultSuccessNoData());
            boolean hasSuccessWithData = StringUtils.isNotEmpty(cs.getRequestResultSuccessWithData());
            boolean anyPresent = hasQualifier || hasTypeDecl || hasSuccessNoData || hasSuccessWithData;
            boolean allPresent = hasQualifier && hasTypeDecl && hasSuccessNoData && hasSuccessWithData;
            if (anyPresent && !allPresent) {
                if (!hasQualifier) {
                    errors.add("codeSnippet.requestResultQualifier must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasTypeDecl) {
                    errors.add("codeSnippet.requestResultTypeDeclaration must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasSuccessNoData) {
                    errors.add("codeSnippet.requestResultSuccessNoData must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasSuccessWithData) {
                    errors.add("codeSnippet.requestResultSuccessWithData must not be empty when any other "
                            + "requestResult field is specified");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new Allison1875Exception(
                    "Allison 1875 fail to work cause invalid config\n" + String.join("\n", errors));
        }
    }

}
```

- [ ] **Step 2: Verify it compiles (expect errors — DomainConfig not yet updated)**

Run: `mvn compile -pl common -am 2>&1 | tail -5`
Expected: Compilation errors related to DomainConfig (expected, will fix in Task 3)

---

### Task 3: Rewrite DomainConfig as immutable @Value class

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/config/DomainConfig.java`

- [ ] **Step 1: Rewrite DomainConfig.java**

Replace the entire file with:

```java
package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

/**
 * 业务领域配置，描述一个业务领域中各层次代码的模块位置和包名。
 *
 * @author Deolin 2026-04-28
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class DomainConfig {

    String name;

    // ==================== 控制层 ====================

    String controllerModule;

    String controllerPackage;

    // ==================== DTO层 ====================

    String dtoModule;

    String reqDTOPackage;

    String respDTOPackage;

    // ==================== 枚举层 ====================

    String enumModule;

    String enumPackage;

    // ==================== 业务层 Service ====================

    String serviceModule;

    String servicePackage;

    // ==================== 业务层 ServiceImpl ====================

    String serviceImplModule;

    String serviceImplPackage;

    // ==================== 持久层 ====================

    String persistenceModule;

    String mapperPackage;

    String entityPackage;

    String designPackage;

    String paramDTOPackage;

    String recordDTOPackage;

    List<File> mapperXmlDirs;

    String wholeDTOPackage;

    // ==================== 解析后路径（非 YAML 配置） ====================

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path controllerSourceRoot;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path dtoSourceRoot;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path enumSourceRoot;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path serviceSourceRoot;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path serviceImplSourceRoot;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path persistenceSourceRoot;

    static DomainConfig applyDefaults(DomainConfig raw) {
        if (raw == null) {
            return DomainConfig.builder().build();
        }
        return raw.toBuilder()
                .mapperXmlDirs(raw.getMapperXmlDirs() != null ? raw.getMapperXmlDirs()
                        : List.of(new File("src/main/resources/mapper")))
                .build();
    }

    static void validate(DomainConfig dc, String prefix, List<String> errors) {
        if (StringUtils.isEmpty(dc.getName())) {
            errors.add(prefix + ".name must not be empty");
        }
        if (StringUtils.isEmpty(dc.getControllerModule())) {
            errors.add(prefix + ".controllerModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getControllerPackage())) {
            errors.add(prefix + ".controllerPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getDtoModule())) {
            errors.add(prefix + ".dtoModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getReqDTOPackage())) {
            errors.add(prefix + ".reqDTOPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getRespDTOPackage())) {
            errors.add(prefix + ".respDTOPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getEnumModule())) {
            errors.add(prefix + ".enumModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getEnumPackage())) {
            errors.add(prefix + ".enumPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getServiceModule())) {
            errors.add(prefix + ".serviceModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getServicePackage())) {
            errors.add(prefix + ".servicePackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getServiceImplModule())) {
            errors.add(prefix + ".serviceImplModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getServiceImplPackage())) {
            errors.add(prefix + ".serviceImplPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getPersistenceModule())) {
            errors.add(prefix + ".persistenceModule must not be empty");
        }
        if (StringUtils.isEmpty(dc.getMapperPackage())) {
            errors.add(prefix + ".mapperPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getEntityPackage())) {
            errors.add(prefix + ".entityPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getDesignPackage())) {
            errors.add(prefix + ".designPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getParamDTOPackage())) {
            errors.add(prefix + ".paramDTOPackage must not be empty");
        }
        if (StringUtils.isEmpty(dc.getRecordDTOPackage())) {
            errors.add(prefix + ".recordDTOPackage must not be empty");
        }
        if (dc.getMapperXmlDirs() == null || dc.getMapperXmlDirs().isEmpty()) {
            errors.add(prefix + ".mapperXmlDirs must not be empty");
        }
        if (StringUtils.isEmpty(dc.getWholeDTOPackage())) {
            errors.add(prefix + ".wholeDTOPackage must not be empty");
        }
    }

}
```

- [ ] **Step 2: Verify common module compiles**

Run: `mvn compile -pl common -am`
Expected: BUILD SUCCESS (Config and DomainConfig now compile together)

---

### Task 4: Delete ConfigValid and ConfigValidator

**Files:**
- Delete: `common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValid.java`
- Delete: `common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValidator.java`

- [ ] **Step 1: Delete ConfigValid.java**

```bash
rm common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValid.java
```

- [ ] **Step 2: Delete ConfigValidator.java**

```bash
rm common/src/main/java/com/spldeolin/allison1875/common/config/ConfigValidator.java
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl common -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit Tasks 2-4 together**

```bash
git add -A
git commit -m "$(cat <<'EOF'
refactor!: make Config and DomainConfig immutable

1. Rewrite Config with @Value/@Jacksonized/@Builder(toBuilder=true)
2. Add static factory Config.fromYaml(File) with deserialization, defaults, and validation
3. Rewrite DomainConfig with same immutable pattern
4. Delete ConfigValid annotation and ConfigValidator class
5. Validation logic moved from annotation-based to procedural Config.validate()

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: Update Allison1875.java to use immutable patterns

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/Allison1875.java`

- [ ] **Step 1: Update resolveSourceRoots to return new instance**

Change `resolveSourceRoots` from void-mutating to returning a new `DomainConfig`:

```java
    private static DomainConfig resolveSourceRoots(DomainConfig domainConfig) {
        Path persistenceModule = Paths.get(domainConfig.getPersistenceModule());
        return domainConfig.toBuilder()
                .controllerSourceRoot(Paths.get(domainConfig.getControllerModule(), "src/main/java"))
                .dtoSourceRoot(Paths.get(domainConfig.getDtoModule(), "src/main/java"))
                .enumSourceRoot(Paths.get(domainConfig.getEnumModule(), "src/main/java"))
                .serviceSourceRoot(Paths.get(domainConfig.getServiceModule(), "src/main/java"))
                .serviceImplSourceRoot(Paths.get(domainConfig.getServiceImplModule(), "src/main/java"))
                .persistenceSourceRoot(Paths.get(domainConfig.getPersistenceModule(), "src/main/java"))
                .mapperXmlDirs(domainConfig.getMapperXmlDirs().stream()
                        .map(dir -> persistenceModule.resolve(dir.toPath()).toFile())
                        .collect(Collectors.toList()))
                .build();
    }
```

- [ ] **Step 2: Update prepareDomain to use the returned instance**

Change `prepareDomain` method:

```java
    public static void prepareDomain(Config config, String domainName) {
        DomainConfig domainConfig = resolveDomain(config, domainName);
        log.info("targetDomain={}", JsonUtils.toJson(domainConfig));
        DomainConfig resolved = resolveSourceRoots(domainConfig);
        DomainContext.set(resolved);
    }
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl common -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/Allison1875.java
git commit -m "$(cat <<'EOF'
refactor: make resolveSourceRoots return new DomainConfig instance

1. resolveSourceRoots returns new immutable DomainConfig via toBuilder()
2. prepareDomain stores resolved instance in DomainContext

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: Update Entrypoint to use Config.fromYaml

**Files:**
- Modify: `allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Entrypoint.java`

- [ ] **Step 1: Replace loadConfig with Config.fromYaml**

Replace the `loadConfig` method and its call site. The full updated file:

```java
package com.spldeolin.allison1875.cli;

import java.io.File;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * CLI 主入口。
 *
 * @author Deolin 2026-05-10
 */
@Slf4j
public class Entrypoint {

    public static void main(String[] args) {
        Allison1875.hello();

        CliArgs cliArgs = parseArgs(args);
        log.info("toolName={} domainName={} configFile={}", cliArgs.tool, cliArgs.domainName, cliArgs.configFile);

        Config config = Config.fromYaml(new File(cliArgs.configFile));
        log.info("config={}", JsonUtils.toJson(config));

        Allison1875.letsGo(cliArgs.tool, config, cliArgs.domainName);
    }

    private static CliArgs parseArgs(String[] args) {
        String tool = null;
        String domainName = null;
        String configFilePath = null;
        for (String arg : args) {
            if (arg.startsWith("--tool=")) {
                tool = arg.substring("--tool=".length());
            } else if (arg.startsWith("--domain=")) {
                domainName = arg.substring("--domain=".length());
            } else if (arg.startsWith("--config=")) {
                configFilePath = arg.substring("--config=".length());
            }
        }
        if (tool == null || tool.isEmpty()) {
            throw new Allison1875Exception("必须通过 --tool=<toolName> 指定工具名（如 doc-analyzer）");
        }
        if (configFilePath == null || configFilePath.isEmpty()) {
            throw new Allison1875Exception("必须通过 --config=<path> 指定 .allison1875.yml 配置文件路径");
        }
        CliArgs cliArgs = new CliArgs();
        cliArgs.tool = ToolEnum.of(tool);
        cliArgs.domainName = domainName;
        cliArgs.configFile = configFilePath;
        return cliArgs;
    }

    private static class CliArgs {

        ToolEnum tool;

        String domainName;

        String configFile;

    }

}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl allison1875-cli -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Entrypoint.java
git commit -m "$(cat <<'EOF'
refactor: replace SnakeYaml loadConfig with Config.fromYaml

1. Remove SnakeYaml deserialization from Entrypoint
2. Use Config.fromYaml(File) static factory method

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 7: Update FormGenerator to use toBuilder + new Injector

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java`

- [ ] **Step 1: Refactor FormGenerator.play() to use derived Config**

The key changes:
1. Replace `config.setJdbcUrl(null); config.setDdl(ddl); config.setEnableGenerateDesign(true);` with `toBuilder()` + new Injector for persistence-generator
2. Replace `config.setMvcHandlerQualifierWildcards(controllerQualifiers);` with `toBuilder()` + new Injector for doc-analyzer
3. The `@Inject private PersistenceGenerator`, `@Inject private DocAnalyzer`, `@Inject private QueryTransformer` fields can remain if we inject them from the same Guice context, but since we now need _different_ Config for sub-tools, we must construct them separately

Replace the `play()` method body. The key section (lines 121-191) becomes:

```java
        // 生成持久层 (with derived config)
        Config pgConfig = config.toBuilder()
                .jdbcUrl(null)
                .ddl(ddl)
                .enableGenerateDesign(true)
                .build();
        Injector pgInjector = Guice.createInjector(
                new PersistenceGeneratorModule(pgConfig), new ValidationModule());
        pgInjector.getInstance(PersistenceGenerator.class).play();

        // 生成枚举
        enumService.generateEnums(forms);

        // 生成controller和initDec
        List<String> controllerQualifiers = Lists.newArrayList();
        for (FormDef form : forms) {
            // ... existing controller generation code unchanged ...
        }

        // 调用handler-transformer
        handlerTransformer.play();

        // mvn compile
        MavenUtils.compile(new File(DomainContext.get().getControllerModule()), config.getJavaHome());

        // 设置可排序字段
        if (mapperLayerExpansionService instanceof FormGeneratorMapperLayerExpansionServiceImpl) {
            List<ItemDef> allSortableItems = Lists.newArrayList();
            for (FormDef form : forms) {
                for (ItemDef item : form.getItems()) {
                    if (item.getType() == ItemType.NUMBER || item.getType() == ItemType.ON_OFF
                            || item.getType() == ItemType.TEXT || item.getType() == ItemType.TIME) {
                        allSortableItems.add(item);
                    }
                }
            }
            ((FormGeneratorMapperLayerExpansionServiceImpl) mapperLayerExpansionService).setSortableItems(allSortableItems);
        }

        // 调用query-transformer转换Design Chain
        queryTransformer.play();

        // 调用doc-analyzer (with derived config)
        Config daConfig = config.toBuilder()
                .mvcHandlerQualifierWildcards(controllerQualifiers)
                .build();
        Injector daInjector = Guice.createInjector(
                new DocAnalyzerModule(daConfig), new ValidationModule());
        daInjector.getInstance(DocAnalyzer.class).play();
```

Also add the necessary imports:
```java
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spldeolin.allison1875.common.guice.ValidationModule;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule;
```

And remove the `@Inject private PersistenceGenerator persistenceGenerator;` and `@Inject private DocAnalyzer docAnalyzer;` fields (they are now created from separate Injectors).

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl form-generator -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java
git commit -m "$(cat <<'EOF'
refactor: FormGenerator uses toBuilder + new Injector for sub-tools

1. Derive pgConfig via toBuilder() for persistence-generator
2. Derive daConfig via toBuilder() for doc-analyzer
3. Construct separate Guice Injectors for sub-tools with derived configs
4. Remove @Inject fields for PersistenceGenerator and DocAnalyzer

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 8: Update AppGenerator to use Config.builder() instead of new Config()

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Replace new Config() and setter calls with builder**

In `invokeFormGenerator()` method (around line 330-369), replace:

```java
        Config fgConfig = new Config();
        fgConfig.setDslPath(tempDsl.toFile());
        fgConfig.setAuthor(config.getAuthor());
        fgConfig.setJdbcUrl(null);
        fgConfig.setEnableGenerateDesign(true);
        fgConfig.setIsEntityEndWithEntity(true);
        fgConfig.setEnableJavaxMoveToJakarta(false);
        fgConfig.setEnableOneService(true);
        fgConfig.setMarkdownDir(new File(absPath + "/api-docs"));

        Config.CodeSnippet cs = new Config.CodeSnippet();
        String ns = appDef.getNamespace();
        cs.setRequestResultQualifier(ns + ".common.RequestResult");
        cs.setRequestResultTypeDeclaration("RequestResult<${dataType}>");
        cs.setRequestResultSuccessNoData("RequestResult.success()");
        cs.setRequestResultSuccessWithData("RequestResult.success(${data})");
        cs.setBizExceptionQualifier(ns + ".common.BizException");
        fgConfig.setCodeSnippet(cs);

        DomainConfig dc = new DomainConfig();
        dc.setName("default");
        dc.setControllerModule(absPath);
        dc.setControllerPackage(ns + ".controller");
        dc.setDtoModule(absPath);
        dc.setReqDTOPackage(ns + ".dto.req");
        dc.setRespDTOPackage(ns + ".dto.resp");
        dc.setEnumModule(absPath);
        dc.setEnumPackage(ns + ".enums");
        dc.setServiceModule(absPath);
        dc.setServicePackage(ns + ".service");
        dc.setServiceImplModule(absPath);
        dc.setServiceImplPackage(ns + ".service.impl");
        dc.setPersistenceModule(absPath);
        dc.setMapperPackage(ns + ".mapper");
        dc.setEntityPackage(ns + ".entity");
        dc.setDesignPackage(ns + ".design");
        dc.setParamDTOPackage(ns + ".dto.param");
        dc.setRecordDTOPackage(ns + ".dto.record");
        dc.setWholeDTOPackage(ns + ".dto");
        fgConfig.setDomains(Lists.newArrayList(dc));
```

With:

```java
        String ns = appDef.getNamespace();

        Config.CodeSnippet cs = Config.CodeSnippet.builder()
                .requestResultQualifier(ns + ".common.RequestResult")
                .requestResultTypeDeclaration("RequestResult<${dataType}>")
                .requestResultSuccessNoData("RequestResult.success()")
                .requestResultSuccessWithData("RequestResult.success(${data})")
                .bizExceptionQualifier(ns + ".common.BizException")
                .controllerRequestMapping("/api/v1/${formName}")
                .shortUuidGeneration("UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()")
                .collectionEmptyCheck("${list} == null || ${list}.isEmpty()")
                .build();

        DomainConfig dc = DomainConfig.builder()
                .name("default")
                .controllerModule(absPath)
                .controllerPackage(ns + ".controller")
                .dtoModule(absPath)
                .reqDTOPackage(ns + ".dto.req")
                .respDTOPackage(ns + ".dto.resp")
                .enumModule(absPath)
                .enumPackage(ns + ".enums")
                .serviceModule(absPath)
                .servicePackage(ns + ".service")
                .serviceImplModule(absPath)
                .serviceImplPackage(ns + ".service.impl")
                .persistenceModule(absPath)
                .mapperPackage(ns + ".mapper")
                .entityPackage(ns + ".entity")
                .designPackage(ns + ".design")
                .paramDTOPackage(ns + ".dto.param")
                .recordDTOPackage(ns + ".dto.record")
                .wholeDTOPackage(ns + ".dto")
                .mapperXmlDirs(List.of(new File("src/main/resources/mapper")))
                .build();

        Config fgConfig = Config.builder()
                .dslPath(tempDsl.toFile())
                .author(config.getAuthor())
                .enableGenerateDesign(true)
                .isEntityEndWithEntity(true)
                .enableJavaxMoveToJakarta(false)
                .enableOneService(true)
                .markdownDir(new File(absPath + "/api-docs"))
                .codeSnippet(cs)
                .domains(Lists.newArrayList(dc))
                .isDataModelWithoutLombok(false)
                .enableNoModifyAnnounce(true)
                .tables(new ArrayList<>())
                .dependencyDirsOrJavaFilePath(new ArrayList<>())
                .globalUrlPrefix("")
                .flushTo(List.of(FlushToEnum.MARKDOWN))
                .singleEndpointPerMarkdown(false)
                .getEnumCodeMethodName("getCode")
                .getEnumTitleMethodName("getTitle")
                .wholeDTONamePostfix("WholeDTO")
                .docAnalyzerModule("com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule")
                .handlerTransformerModule("com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule")
                .persistenceGeneratorModule("com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule")
                .queryTransformerModule("com.spldeolin.allison1875.querytransformer.QueryTransformerModule")
                .starTransformerModule("com.spldeolin.allison1875.startransformer.StarTransformerModule")
                .formGeneratorModule("com.spldeolin.allison1875.formgenerator.FormGeneratorModule")
                .appGeneratorModule("com.spldeolin.allison1875.appgenerator.AppGeneratorModule")
                .build();
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl app-generator -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java
git commit -m "$(cat <<'EOF'
refactor: AppGenerator uses Config.builder() and DomainConfig.builder()

1. Replace new Config() + setters with Config.builder()...build()
2. Replace new DomainConfig() + setters with DomainConfig.builder()...build()
3. Replace new CodeSnippet() + setters with CodeSnippet.builder()...build()

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 9: Fix remaining compilation errors across all modules

**Files:**
- Potentially modify any module that calls Config/DomainConfig setters or accesses removed annotations

- [ ] **Step 1: Attempt full project compilation**

Run: `mvn compile`
Expected: May have errors in IT base tests or other modules that reference Config setters

- [ ] **Step 2: Fix any remaining setter usages**

Search for remaining calls:
```bash
grep -rn "config\.set\|new Config()\|new DomainConfig()" --include="*.java" | grep -v target
```

For each hit, convert setter chains to builder patterns. Common patterns:
- `new Config()` → `Config.builder()...build()`
- `new DomainConfig()` → `DomainConfig.builder()...build()`
- `config.setXxx(...)` → must use `toBuilder()` or rebuild

- [ ] **Step 3: Fix IT base tests that construct Config/DomainConfig**

IT base tests in `allison1875-cli/src/test/java/` likely use SnakeYaml to load `.allison1875.yml` and then modify paths. These need to use `Config.fromYaml(file)` followed by `toBuilder()` to override paths.

- [ ] **Step 4: Verify full compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
refactor: fix remaining Config/DomainConfig compilation errors

1. Convert all remaining setter usages to builder pattern
2. Update IT base tests to use Config.fromYaml + toBuilder

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 10: Remove SnakeYaml dependency from CLI module

**Files:**
- Modify: `allison1875-cli/pom.xml` (if SnakeYaml is declared — it's not explicitly declared, it comes transitively from JavaParser's snakeyaml-engine or Jackson)

- [ ] **Step 1: Check if SnakeYaml is explicitly declared**

```bash
grep -n "snakeyaml" allison1875-cli/pom.xml common/pom.xml pom.xml
```

If not found as a direct dependency (which is the case based on our earlier scan), no POM changes needed. SnakeYaml remains as a transitive dependency of other libraries but is no longer directly used.

- [ ] **Step 2: Verify no SnakeYaml imports remain in production code**

```bash
grep -rn "org.yaml.snakeyaml" --include="*.java" | grep -v target | grep -v test
```

Expected: No results (Entrypoint was the only direct user)

- [ ] **Step 3: Commit (if changes were needed)**

Skip commit if no changes.

---

### Task 11: Write unit tests for Config

**Files:**
- Create: `common/src/test/java/com/spldeolin/allison1875/common/config/ConfigTest.java`
- Create: `common/src/test/resources/config/full-config.yml`
- Create: `common/src/test/resources/config/minimal-config.yml`
- Create: `common/src/test/resources/config/empty-domains.yml`
- Create: `common/src/test/resources/config/invalid-jdbc.yml`
- Create: `common/src/test/resources/config/invalid-yapi.yml`
- Create: `common/src/test/resources/config/invalid-code-snippet.yml`
- Create: `common/src/test/resources/config/invalid-domain-fields.yml`

- [ ] **Step 1: Create test YAML fixtures**

`common/src/test/resources/config/full-config.yml`:
```yaml
author: "Test Author"
isDataModelWithoutLombok: true
enableNoModifyAnnounce: false
enableJavaxMoveToJakarta: true
enableOneService: true
jdbcUrl: "jdbc:mysql://localhost:3306/test"
userName: "root"
password: "123456"
schema: "test_db"
enableGenerateDesign: false
isEntityEndWithEntity: false
wholeDTONamePostfix: "DTO"
globalUrlPrefix: "/api"
getEnumCodeMethodName: "getValue"
getEnumTitleMethodName: "getLabel"
domains:
  - name: "order"
    controllerModule: "/tmp/order"
    controllerPackage: "com.example.controller"
    dtoModule: "/tmp/order"
    reqDTOPackage: "com.example.dto.req"
    respDTOPackage: "com.example.dto.resp"
    enumModule: "/tmp/order"
    enumPackage: "com.example.enums"
    serviceModule: "/tmp/order"
    servicePackage: "com.example.service"
    serviceImplModule: "/tmp/order"
    serviceImplPackage: "com.example.service.impl"
    persistenceModule: "/tmp/order"
    mapperPackage: "com.example.mapper"
    entityPackage: "com.example.entity"
    designPackage: "com.example.design"
    paramDTOPackage: "com.example.dto.param"
    recordDTOPackage: "com.example.dto.record"
    wholeDTOPackage: "com.example.dto"
    mapperXmlDirs:
      - "src/main/resources/mapper"
```

`common/src/test/resources/config/minimal-config.yml`:
```yaml
domains:
  - name: "default"
    controllerModule: "/tmp/proj"
    controllerPackage: "com.example.controller"
    dtoModule: "/tmp/proj"
    reqDTOPackage: "com.example.dto.req"
    respDTOPackage: "com.example.dto.resp"
    enumModule: "/tmp/proj"
    enumPackage: "com.example.enums"
    serviceModule: "/tmp/proj"
    servicePackage: "com.example.service"
    serviceImplModule: "/tmp/proj"
    serviceImplPackage: "com.example.service.impl"
    persistenceModule: "/tmp/proj"
    mapperPackage: "com.example.mapper"
    entityPackage: "com.example.entity"
    designPackage: "com.example.design"
    paramDTOPackage: "com.example.dto.param"
    recordDTOPackage: "com.example.dto.record"
    wholeDTOPackage: "com.example.dto"
```

`common/src/test/resources/config/empty-domains.yml`:
```yaml
domains: []
```

`common/src/test/resources/config/invalid-jdbc.yml`:
```yaml
jdbcUrl: "jdbc:mysql://localhost:3306/test"
domains:
  - name: "default"
    controllerModule: "/tmp/proj"
    controllerPackage: "com.example.controller"
    dtoModule: "/tmp/proj"
    reqDTOPackage: "com.example.dto.req"
    respDTOPackage: "com.example.dto.resp"
    enumModule: "/tmp/proj"
    enumPackage: "com.example.enums"
    serviceModule: "/tmp/proj"
    servicePackage: "com.example.service"
    serviceImplModule: "/tmp/proj"
    serviceImplPackage: "com.example.service.impl"
    persistenceModule: "/tmp/proj"
    mapperPackage: "com.example.mapper"
    entityPackage: "com.example.entity"
    designPackage: "com.example.design"
    paramDTOPackage: "com.example.dto.param"
    recordDTOPackage: "com.example.dto.record"
    wholeDTOPackage: "com.example.dto"
```

`common/src/test/resources/config/invalid-yapi.yml`:
```yaml
flushTo:
  - YAPI
domains:
  - name: "default"
    controllerModule: "/tmp/proj"
    controllerPackage: "com.example.controller"
    dtoModule: "/tmp/proj"
    reqDTOPackage: "com.example.dto.req"
    respDTOPackage: "com.example.dto.resp"
    enumModule: "/tmp/proj"
    enumPackage: "com.example.enums"
    serviceModule: "/tmp/proj"
    servicePackage: "com.example.service"
    serviceImplModule: "/tmp/proj"
    serviceImplPackage: "com.example.service.impl"
    persistenceModule: "/tmp/proj"
    mapperPackage: "com.example.mapper"
    entityPackage: "com.example.entity"
    designPackage: "com.example.design"
    paramDTOPackage: "com.example.dto.param"
    recordDTOPackage: "com.example.dto.record"
    wholeDTOPackage: "com.example.dto"
```

`common/src/test/resources/config/invalid-code-snippet.yml`:
```yaml
codeSnippet:
  requestResultQualifier: "com.example.Result"
domains:
  - name: "default"
    controllerModule: "/tmp/proj"
    controllerPackage: "com.example.controller"
    dtoModule: "/tmp/proj"
    reqDTOPackage: "com.example.dto.req"
    respDTOPackage: "com.example.dto.resp"
    enumModule: "/tmp/proj"
    enumPackage: "com.example.enums"
    serviceModule: "/tmp/proj"
    servicePackage: "com.example.service"
    serviceImplModule: "/tmp/proj"
    serviceImplPackage: "com.example.service.impl"
    persistenceModule: "/tmp/proj"
    mapperPackage: "com.example.mapper"
    entityPackage: "com.example.entity"
    designPackage: "com.example.design"
    paramDTOPackage: "com.example.dto.param"
    recordDTOPackage: "com.example.dto.record"
    wholeDTOPackage: "com.example.dto"
```

`common/src/test/resources/config/invalid-domain-fields.yml`:
```yaml
domains:
  - name: "broken"
    controllerModule: "/tmp/proj"
```

- [ ] **Step 2: Write ConfigTest.java**

```java
package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.util.List;
import java.util.Objects;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Deolin 2026-06-29
 */
class ConfigTest {

    private File fixture(String name) {
        return new File(Objects.requireNonNull(
                getClass().getClassLoader().getResource("config/" + name)).getFile());
    }

    @Test
    void shouldDeserializeFullConfig() {
        Config config = Config.fromYaml(fixture("full-config.yml"));

        assertEquals("Test Author", config.getAuthor());
        assertTrue(config.getIsDataModelWithoutLombok());
        assertFalse(config.getEnableNoModifyAnnounce());
        assertTrue(config.getEnableJavaxMoveToJakarta());
        assertTrue(config.getEnableOneService());
        assertEquals("jdbc:mysql://localhost:3306/test", config.getJdbcUrl());
        assertEquals("root", config.getUserName());
        assertEquals("123456", config.getPassword());
        assertEquals("test_db", config.getSchema());
        assertFalse(config.getEnableGenerateDesign());
        assertFalse(config.getIsEntityEndWithEntity());
        assertEquals("DTO", config.getWholeDTONamePostfix());
        assertEquals("/api", config.getGlobalUrlPrefix());
        assertEquals("getValue", config.getGetEnumCodeMethodName());
        assertEquals("getLabel", config.getGetEnumTitleMethodName());

        assertEquals(1, config.getDomains().size());
        DomainConfig dc = config.getDomains().get(0);
        assertEquals("order", dc.getName());
        assertEquals("/tmp/order", dc.getControllerModule());
        assertEquals("com.example.controller", dc.getControllerPackage());
    }

    @Test
    void shouldApplyDefaults() {
        Config config = Config.fromYaml(fixture("minimal-config.yml"));

        assertEquals("Allison 1875", config.getAuthor());
        assertFalse(config.getIsDataModelWithoutLombok());
        assertTrue(config.getEnableNoModifyAnnounce());
        assertFalse(config.getEnableJavaxMoveToJakarta());
        assertFalse(config.getEnableOneService());
        assertTrue(config.getEnableGenerateDesign());
        assertTrue(config.getIsEntityEndWithEntity());
        assertEquals("WholeDTO", config.getWholeDTONamePostfix());
        assertEquals("", config.getGlobalUrlPrefix());
        assertEquals(List.of(FlushToEnum.MARKDOWN), config.getFlushTo());
        assertEquals(new File("api-docs"), config.getMarkdownDir());
        assertEquals("doc-analyzer", config.getShowdocBaseCatName());
        assertEquals(new File("api-dsls"), config.getDslDir());
        assertFalse(config.getSingleEndpointPerMarkdown());
        assertEquals("getCode", config.getGetEnumCodeMethodName());
        assertEquals("getTitle", config.getGetEnumTitleMethodName());
        assertEquals(new File("./app.yml"), config.getAppDslPath());
        assertEquals(new File("./output"), config.getAppGeneratorOutputDir());
        assertEquals(new File("./forms.yml"), config.getDslPath());
        assertNotNull(config.getCodeSnippet());
        assertEquals("/api/v1/${formName}", config.getCodeSnippet().getControllerRequestMapping());
        assertEquals("java.lang.RuntimeException", config.getCodeSnippet().getBizExceptionQualifier());

        // DomainConfig defaults
        DomainConfig dc = config.getDomains().get(0);
        assertEquals(List.of(new File("src/main/resources/mapper")), dc.getMapperXmlDirs());
    }

    @Test
    void shouldFailWhenDomainsEmpty() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("empty-domains.yml")));
        assertTrue(ex.getMessage().contains("domains must not be empty"));
    }

    @Test
    void shouldFailWhenJdbcUrlWithoutCredentials() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-jdbc.yml")));
        assertTrue(ex.getMessage().contains("userName must not be empty"));
        assertTrue(ex.getMessage().contains("password must not be empty"));
        assertTrue(ex.getMessage().contains("schema must not be empty"));
    }

    @Test
    void shouldFailWhenYapiFlushToWithoutUrl() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-yapi.yml")));
        assertTrue(ex.getMessage().contains("yapiUrl must not be null"));
        assertTrue(ex.getMessage().contains("yapiToken must not be null"));
    }

    @Test
    void shouldFailWhenCodeSnippetPartiallyFilled() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-code-snippet.yml")));
        assertTrue(ex.getMessage().contains("requestResultTypeDeclaration must not be empty"));
        assertTrue(ex.getMessage().contains("requestResultSuccessNoData must not be empty"));
        assertTrue(ex.getMessage().contains("requestResultSuccessWithData must not be empty"));
    }

    @Test
    void shouldDeriveConfigWithToBuilder() {
        Config original = Config.fromYaml(fixture("minimal-config.yml"));
        Config derived = original.toBuilder()
                .ddl("CREATE TABLE t (id BIGINT)")
                .jdbcUrl(null)
                .enableGenerateDesign(true)
                .build();

        // Original unchanged
        assertNull(original.getDdl());
        // Derived has new values
        assertEquals("CREATE TABLE t (id BIGINT)", derived.getDdl());
        assertNull(derived.getJdbcUrl());
        assertTrue(derived.getEnableGenerateDesign());
        // Other fields preserved
        assertEquals("Allison 1875", derived.getAuthor());
    }

    @Test
    void shouldValidateDomainConfigRequiredFields() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-domain-fields.yml")));
        assertTrue(ex.getMessage().contains("controllerPackage must not be empty"));
        assertTrue(ex.getMessage().contains("dtoModule must not be empty"));
    }

}
```

- [ ] **Step 3: Run tests**

Run: `mvn test -pl common -Dtest=ConfigTest`
Expected: All 8 tests PASS

- [ ] **Step 4: Commit**

```bash
git add common/src/test/java/com/spldeolin/allison1875/common/config/ConfigTest.java common/src/test/resources/config/
git commit -m "$(cat <<'EOF'
test: add unit tests for immutable Config module

1. Test full deserialization, defaults application, validation failures
2. Test toBuilder derivative, DomainConfig cascade validation
3. Add YAML test fixtures under common/src/test/resources/config/

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 12: Run full test suite and fix regressions

**Files:**
- Potentially modify IT base tests and any other files with compilation or runtime errors

- [ ] **Step 1: Run full test suite**

Run: `mvn verify`
Expected: All tests pass. If not, proceed to fix.

- [ ] **Step 2: Fix IT base test Config loading**

The IT base tests in `allison1875-cli/src/test/java/.../it/` load `.allison1875.yml` and modify paths. They likely need updating from SnakeYaml to `Config.fromYaml()` + `toBuilder()` for path overrides.

Check what the base tests do:
```bash
grep -rn "snakeyaml\|Yaml\|Config\|loadConfig" allison1875-cli/src/test/java/ --include="*.java" | grep -i "base\|Base"
```

Update base tests to:
1. Use `Config.fromYaml(ymlFile)` instead of SnakeYaml
2. Use `config.toBuilder().domains(updatedDomains).build()` for path rewrites
3. Use `domainConfig.toBuilder().controllerModule(absPath).build()` for individual domain path rewrites

- [ ] **Step 3: Run full test suite again**

Run: `mvn verify`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
fix: update IT base tests for immutable Config

1. Replace SnakeYaml loading with Config.fromYaml()
2. Use toBuilder() for path rewrites in test setup

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 13: Update CLAUDE.md documentation

**Files:**
- Modify: `CLAUDE.md` (root)
- Modify: `common/CLAUDE.md`

- [ ] **Step 1: Update root CLAUDE.md**

In the "关键安全规则 > 必须遵守" section, remove or update references to `@ConfigValid`. In the "代码风格 > DTO 模板" section, note that Config classes use `@Value`/`@Builder` not `@Data`.

In "代码风格 > Lombok 用法" section, add:
```
- Config: `@Value`, `@Jacksonized`, `@Builder(toBuilder = true)`
```

- [ ] **Step 2: Update common/CLAUDE.md**

Replace the "Config Validation" section with:

```markdown
## Config 构造

- `Config` 和 `DomainConfig` 使用 `@Value` + `@Jacksonized` + `@Builder(toBuilder = true)`，反序列化后不可变
- `Config.fromYaml(File)` 是唯一的构造入口：反序列化 → 应用默认值 → 校验
- 运行时需要覆盖配置时使用 `config.toBuilder().field(newValue).build()` 派生副本
- 不再使用 `@ConfigValid` 注解和 `ConfigValidator` 类
- `ValidSingletonListener` 仍服务于其它 Guice bean 的校验
```

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md common/CLAUDE.md
git commit -m "$(cat <<'EOF'
docs: update CLAUDE.md for immutable Config pattern

1. Document @Value/@Jacksonized/@Builder pattern for Config
2. Replace Config Validation section in common/CLAUDE.md
3. Add Config to Lombok usage table

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```
