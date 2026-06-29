# Common 模块 — DI 与 AST 模式参考

## DI 模式模板

```java
// 接口
@ImplementedBy(XxxServiceImpl.class)
public interface XxxService {

    ReturnType methodName(ArgType arg);

}

// 实现
@Singleton
@Slf4j
public class XxxServiceImpl implements XxxService {

    @Inject
    private Config config;

    @Override
    public ReturnType methodName(ArgType arg) { ...}

}
```

Module 模板：

```java

@Slf4j
@ToString
public class XxxModule extends Allison1875Module {

    private final Config config;

    public XxxModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875Game> declareGameType() {
        return Xxx.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
    }

}
```

## AST 处理管道模板

```java

@Singleton
@Slf4j
public class Xxx implements Allison1875Game {

    @Inject
    private Config config;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void play() {
        for (CompilationUnit cu : AstForestContext.get()) {
            // 1. detect 目标节点
            // 2. analyze / extract
            // 3. generate 新代码
            // 4. modify CU
        }
        // 5. importExprService.extractQualifiedTypeToImport(cu)  ← 必须先于 writeJava
        // 6. CompilationUnitUtils.writeJava(cu)
        // 7. log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE)
    }

}
```

## Config 构造

- `Config` 和 `DomainConfig` 使用 `@Value` + `@Jacksonized` + `@Builder(toBuilder = true)`，反序列化后不可变
- `Config.fromYaml(File)` 是唯一的构造入口：反序列化 → 应用默认值 → 校验
- 运行时需要覆盖配置时使用 `config.toBuilder().field(newValue).build()` 派生副本
- `ValidSingletonListener` 仍服务于其它 Guice bean 的校验
- `ValidationModule` 安装 `ValidSingletonListener`（创建时校验）和 `ValidMethodArgsInterceptor`（方法参数校验）

### Config 覆盖模式（toBuilder + 新 Injector）

当 composite tool（如 form-generator）需要以不同配置调用子 tool 时：

```java
// 派生新 Config
Config pgConfig = config.toBuilder()
        .jdbcUrl(null)
        .ddl(ddl)
        .enableGenerateDesign(true)
        .build();

// 通过反射构建子 tool 的 Module 并创建独立 Injector
Allison1875Module pgModule = (Allison1875Module) Class.forName(config.getPersistenceGeneratorModule())
        .getConstructor(Config.class).newInstance(pgConfig);
Injector pgInjector = Guice.createInjector(pgModule, new ValidationModule());
pgInjector.getInstance(pgModule.declareMainService()).play();
```

### 校验规则

`Config.validate()` 在 `fromYaml()` 内部自动执行，失败抛 `Allison1875Exception`：

1. `domains` 不能为空
2. persistence-generator：`jdbcUrl` 非空时要求 `userName`、`password`、`schema` 同时非空
3. doc-analyzer：`flushTo` 包含 YAPI 时要求 `yapiUrl`+`yapiToken`；包含 MARKDOWN 要求 `markdownDir`；包含 DSL 要求 `dslDir`；包含 SHOWDOC 要求 `showdocUrl`+`showdocApiKey`+`showdocApiToken`
4. codeSnippet：`requestResult*` 四个字段全有或全无
5. DomainConfig：级联校验所有必填字段（name、各 *Module、各 *Package、mapperXmlDirs、wholeDTOPackage）

### Config 默认值速查表

以下字段在 `.allison1875.yml` 中可省略，`applyDefaults()` 会填充默认值：

| 字段 | 默认值 |
|------|--------|
| author | `"Allison 1875"` |
| isDataModelWithoutLombok | `false` |
| enableNoModifyAnnounce | `true` |
| enableJavaxMoveToJakarta | `false` |
| enableOneService | `false` |
| tables | `[]` |
| enableGenerateDesign | `true` |
| isEntityEndWithEntity | `true` |
| wholeDTONamePostfix | `"WholeDTO"` |
| dependencyDirsOrJavaFilePath | `[]` |
| globalUrlPrefix | `""` |
| flushTo | `[MARKDOWN]` |
| markdownDir | `api-docs` |
| showdocBaseCatName | `"doc-analyzer"` |
| dslDir | `api-dsls` |
| singleEndpointPerMarkdown | `false` |
| getEnumCodeMethodName | `"getCode"` |
| getEnumTitleMethodName | `"getTitle"` |
| appDslPath | `./app.yml` |
| appGeneratorOutputDir | `./output` |
| dslPath | `./forms.yml` |
| 各 *Module | 对应模块的标准全限定类名 |

CodeSnippet 默认值：

| 字段 | 默认值 |
|------|--------|
| controllerRequestMapping | `"/api/v1/${formName}"` |
| shortUuidGeneration | `"UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()"` |
| collectionEmptyCheck | `"${list} == null || ${list}.isEmpty()"` |
| bizExceptionQualifier | `"java.lang.RuntimeException"` |

DomainConfig 默认值：

| 字段 | 默认值 |
|------|--------|
| mapperXmlDirs | `[src/main/resources/mapper]` |

### DomainConfig SourceRoot 解析

`Allison1875.resolveSourceRoots(DomainConfig)` 返回新实例（不 mutate），自动从 `*Module` 路径推导 `*SourceRoot`：

- `controllerSourceRoot` = `{controllerModule}/src/main/java`
- `dtoSourceRoot` = `{dtoModule}/src/main/java`
- 其余同理

SourceRoot 字段标注 `@JsonIgnore` + `@EqualsAndHashCode.Exclude` + `@ToString.Exclude`，不参与 YAML 反序列化和对象比较。

## File Snapshot & Rollback

```java
FileSystemSnapshot snapshot = FileSnapshotUtils.createSnapshot(basedir);
try{ /* mutate */ }
        catch(

Throwable e){FileSnapshotUtils.

rollback(snapshot); throw e; }
```
