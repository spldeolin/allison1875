# allison1875-cli IT 测试规范

## 架构

所有集成测试以 JUnit 5 形式运行在 `allison1875-cli` 模块内，通过 in-process 调用 `Entrypoint.main(args)` 执行工具。
JaCoCo 因此可以无需子进程 instrumentation 就覆盖所有 tool 代码路径。

## 目录结构

```
allison1875-cli/src/test/
├── java/.../cli/it/
│   ├── docanalyzer/
│   │   ├── DocAnalyzerItBaseTest.java       # 继承这个
│   │   └── BasicMarkdownItTest.java
│   ├── handlertransformer/
│   │   └── HandlerTransformerItBaseTest.java
│   ├── persistencegenerator/
│   │   └── PersistenceGeneratorItBaseTest.java
│   ├── querytransformer/
│   │   └── ...
│   └── formgenerator/
│       └── ...
└── resources/it/<tool>/<caseName>/
    ├── .allison1875.yml          # *Module 字段用相对路径
    ├── forms.yml                 # form-generator only
    ├── sql/                      # DDL fixtures
    └── src/main/java/...         # 待分析/转换的 Java 源码
```

## 编写新 IT 的规则

1. **必须继承** 对应的 `<Tool>ItBaseTest`，不能直接调 `Entrypoint.main`
2. **资源路径必须相对**：`.allison1875.yml` 中的 `*Module` 字段写相对路径，base class 自动转绝对路径
3. **不要 check in 绝对路径**
4. **`target/it/<caseName>/`** 每次测试重建 — 不跨测试引用

## ItBaseTest 做了什么

1. 递归复制 `src/test/resources/it/<tool>/<caseName>/` → `target/it/<caseName>/`（basedir）
2. 读 `.allison1875.yml`，把相对路径字段改写为基于 basedir 的绝对路径
3. 构造 CLI args 并调 `Entrypoint.main(args)`
4. **保存并恢复线程上下文 ClassLoader**（`DefaultAstForest` 会替换为 IT 作用域的 URLClassLoader）

## 断言模式

```java
@Test
void shouldGenerateExpectedOutput() {
    // base class 已经执行了 Entrypoint.main，结果在 basedir 下
    Path generated = basedir.resolve("src/main/java/com/example/SomeGenerated.java");
    assertTrue(Files.exists(generated));
    String content = Files.readString(generated);
    assertThat(content).contains("expected code snippet");
}
```

## 常用命令

```bash
# 跑全部测试
mvn test

# 跑某个 tool 的全部 IT
mvn test -pl allison1875-cli -am -Dtest="com.spldeolin.allison1875.cli.it.formgenerator.*"

# 跑单个测试类
mvn test -pl allison1875-cli -am -Dtest=CannotInputOnEditItTest

# 跑单个方法
mvn test -pl allison1875-cli -am -Dtest=BasicMarkdownItTest#shouldGenerateMarkdown
```
