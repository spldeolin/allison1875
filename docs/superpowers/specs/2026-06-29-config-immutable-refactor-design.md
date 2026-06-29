# Config Module Immutable Refactor

## Goal

Refactor `com.spldeolin.allison1875.common.config` package to make `Config` and `DomainConfig` immutable after YAML deserialization, replace annotation-based validation with an explicit procedural method, and add unit tests.

## Design Decisions

- **Immutability mechanism:** Lombok `@Value` + `@Jacksonized` + `@Builder(toBuilder = true)`
- **Deserialization:** Switch from SnakeYaml to Jackson YAMLMapper (`jackson-dataformat-yaml` moved from form-generator to common)
- **Validation + defaults:** Single static factory method `Config.fromYaml(File)` that internally deserializes, applies defaults, validates, and returns the final immutable instance
- **Runtime override:** FormGenerator uses `config.toBuilder().ddl(x).build()` to derive a local copy, then constructs a new Guice Injector with the derived Config (same pattern as AppGenerator)
- **DomainConfig:** Also immutable; `resolveSourceRoots()` returns a new instance via `toBuilder()` instead of mutating

## Config Class Structure

```java
@Value
@Jacksonized
@Builder(toBuilder = true)
public class Config {

    List<DomainConfig> domains;
    String author;
    Boolean isDataModelWithoutLombok;
    Boolean enableNoModifyAnnounce;
    Boolean enableJavaxMoveToJakarta;
    String javaHome;

    // Guice Module class names
    String docAnalyzerModule;
    String handlerTransformerModule;
    String persistenceGeneratorModule;
    String queryTransformerModule;
    String starTransformerModule;
    String formGeneratorModule;
    String appGeneratorModule;

    // handler-transformer
    Boolean enableOneService;

    // persistence-generator
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

    // star-transformer
    String wholeDTONamePostfix;

    // doc-analyzer
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

    // app-generator
    File appDslPath;
    File appGeneratorOutputDir;

    // form-generator
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
    }
}
```

## Static Factory Method

```java
public static Config fromYaml(File yamlFile) {
    Config raw = deserialize(yamlFile);
    Config withDefaults = applyDefaults(raw);
    validate(withDefaults);
    return withDefaults;
}
```

### applyDefaults

Uses `toBuilder()` to fill null fields with defaults. Same values as the current field initializers:

| Field | Default |
|-------|---------|
| author | "Allison 1875" |
| isDataModelWithoutLombok | false |
| enableNoModifyAnnounce | true |
| enableJavaxMoveToJakarta | false |
| enableOneService | false |
| tables | empty list |
| enableGenerateDesign | true |
| isEntityEndWithEntity | true |
| wholeDTONamePostfix | "WholeDTO" |
| dependencyDirsOrJavaFilePath | empty list |
| globalUrlPrefix | "" |
| flushTo | [MARKDOWN] |
| markdownDir | new File("api-docs") |
| showdocBaseCatName | "doc-analyzer" |
| dslDir | new File("api-dsls") |
| singleEndpointPerMarkdown | false |
| getEnumCodeMethodName | "getCode" |
| getEnumTitleMethodName | "getTitle" |
| appDslPath | new File("./app.yml") |
| appGeneratorOutputDir | new File("./output") |
| dslPath | new File("./forms.yml") |
| codeSnippet | CodeSnippet with its own defaults |
| Guice Module class names | current default string values |

CodeSnippet defaults:

| Field | Default |
|-------|---------|
| controllerRequestMapping | "/api/v1/${formName}" |
| shortUuidGeneration | "UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()" |
| collectionEmptyCheck | "${list} == null || ${list}.isEmpty()" |
| bizExceptionQualifier | "java.lang.RuntimeException" |

### validate

Procedural validation, migrated from `ConfigValidator.isValid()`:

1. `domains` must not be empty
2. persistence-generator: if `jdbcUrl` or `ddl` is specified, validate mutual constraints (jdbcUrl requires userName/password/schema)
3. doc-analyzer: flushTo-specific field requirements (YAPI → yapiUrl+yapiToken, MARKDOWN → markdownDir, etc.)
4. codeSnippet: requestResult* four fields all-or-nothing
5. DomainConfig: cascade validate each domain's required fields (name, controllerModule, controllerPackage, etc.)

Failure throws `Allison1875Exception` with all error messages joined.

## DomainConfig Structure

```java
@Value
@Jacksonized
@Builder(toBuilder = true)
public class DomainConfig {
    // YAML fields (all required by validation)
    String name;
    String controllerModule;
    String controllerPackage;
    String dtoModule;
    String reqDTOPackage;
    String respDTOPackage;
    String enumModule;
    String enumPackage;
    String serviceModule;
    String servicePackage;
    String serviceImplModule;
    String serviceImplPackage;
    String persistenceModule;
    String mapperPackage;
    String entityPackage;
    String designPackage;
    String paramDTOPackage;
    String recordDTOPackage;
    List<File> mapperXmlDirs;
    String wholeDTOPackage;

    // Resolved at runtime (not from YAML)
    @JsonIgnore
    Path controllerSourceRoot;
    @JsonIgnore
    Path dtoSourceRoot;
    @JsonIgnore
    Path enumSourceRoot;
    @JsonIgnore
    Path serviceSourceRoot;
    @JsonIgnore
    Path serviceImplSourceRoot;
    @JsonIgnore
    Path persistenceSourceRoot;
}
```

### DomainConfig defaults

Applied inside `Config.applyDefaults()` when iterating over domains:

| Field | Default |
|-------|---------|
| mapperXmlDirs | [new File("src/main/resources/mapper")] |

### @JsonIgnore fields and @Value

`@JsonIgnore` on sourceRoot fields means Jackson's builder won't set them from YAML (they remain null until `resolveSourceRoots()` fills them via `toBuilder()`). Since `@Value` includes all fields in `equals`/`hashCode`/`toString`, add `@EqualsAndHashCode.Exclude` and `@ToString.Exclude` on these fields to avoid comparing unresolved vs resolved instances.

## Source Root Resolution

`Allison1875.resolveSourceRoots()` changes from void-mutating to returning a new instance:

```java
private static DomainConfig resolveSourceRoots(DomainConfig raw) {
    Path persistenceModule = Paths.get(raw.getPersistenceModule());
    return raw.toBuilder()
        .controllerSourceRoot(Paths.get(raw.getControllerModule(), "src/main/java"))
        .dtoSourceRoot(Paths.get(raw.getDtoModule(), "src/main/java"))
        .enumSourceRoot(Paths.get(raw.getEnumModule(), "src/main/java"))
        .serviceSourceRoot(Paths.get(raw.getServiceModule(), "src/main/java"))
        .serviceImplSourceRoot(Paths.get(raw.getServiceImplModule(), "src/main/java"))
        .persistenceSourceRoot(Paths.get(raw.getPersistenceModule(), "src/main/java"))
        .mapperXmlDirs(raw.getMapperXmlDirs().stream()
            .map(dir -> persistenceModule.resolve(dir.toPath()).toFile())
            .collect(Collectors.toList()))
        .build();
}
```

## FormGenerator Override Pattern

FormGenerator derives a local Config copy and builds a new Injector (same as AppGenerator pattern):

```java
Config pgConfig = config.toBuilder()
    .jdbcUrl(null)
    .ddl(ddl)
    .enableGenerateDesign(true)
    .build();
// Build new injector with pgConfig for persistence-generator
Injector pgInjector = Guice.createInjector(new PersistenceGeneratorModule(pgConfig), new ValidationModule());
pgInjector.getInstance(PersistenceGenerator.class).play();
```

## Deletions

| Item | Reason |
|------|--------|
| `ConfigValid.java` | Custom validation annotation removed |
| `ConfigValidator.java` | Logic moved to `Config.validate()` |
| All `@NotNull`/`@NotEmpty`/`@Valid` on Config/DomainConfig fields | Validation now procedural |
| Field-level `= defaultValue` initializers | Moved to `applyDefaults()` |
| SnakeYaml deserialization in `Entrypoint.loadConfig()` | Replaced by `Config.fromYaml(file)` |
| SnakeYaml dependency (if declared in common) | Replaced by jackson-dataformat-yaml |

## Retained

- `ValidUtils`, `ValidSingletonListener`, `ValidMethodArgsInterceptor`, `ValidationModule` — still serve other beans
- `DomainContext` — unchanged, stores the current resolved DomainConfig

## Dependency Changes

- `common/pom.xml`: add `jackson-dataformat-yaml`
- `form-generator/pom.xml`: remove `jackson-dataformat-yaml` (now inherited from common)
- `common/pom.xml`: remove SnakeYaml if directly declared (may be transitive from cli)
- `allison1875-cli/pom.xml`: remove SnakeYaml direct dependency

## Unit Tests

**Location:** `common/src/test/java/com/spldeolin/allison1875/common/config/ConfigTest.java`
**Fixtures:** `common/src/test/resources/config/*.yml`

| Test Case | Description |
|-----------|-------------|
| `shouldDeserializeFullConfig` | Complete YAML maps all fields correctly |
| `shouldApplyDefaults` | Minimal YAML (only required fields) gets expected defaults |
| `shouldFailWhenDomainsEmpty` | Throws Allison1875Exception with "domains" error |
| `shouldFailWhenJdbcUrlWithoutCredentials` | jdbcUrl present but missing userName/password/schema |
| `shouldFailWhenYapiFlushToWithoutUrl` | flushTo contains YAPI but yapiUrl/yapiToken missing |
| `shouldFailWhenCodeSnippetPartiallyFilled` | requestResult* fields partially specified |
| `shouldDeriveConfigWithToBuilder` | Original unchanged, derived has overridden fields |
| `shouldValidateDomainConfigRequiredFields` | Missing required DomainConfig fields |
