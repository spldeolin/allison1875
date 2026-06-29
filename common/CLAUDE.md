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
- 不再使用 `@ConfigValid` 注解和 `ConfigValidator` 类
- `ValidSingletonListener` 仍服务于其它 Guice bean 的校验
- `ValidationModule` 安装 `ValidSingletonListener`（创建时校验）和 `ValidMethodArgsInterceptor`（方法参数校验）

## File Snapshot & Rollback

```java
FileSystemSnapshot snapshot = FileSnapshotUtils.createSnapshot(basedir);
try{ /* mutate */ }
        catch(

Throwable e){FileSnapshotUtils.

rollback(snapshot); throw e; }
```
