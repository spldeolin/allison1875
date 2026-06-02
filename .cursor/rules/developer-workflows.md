---
description: Build, install, run tools, .allison1875.yml config sketch, testing (unit + IT), scaffolding new tools/services, and JaCoCo coverage.
globs:
  - pom.xml
  - allison1875-cli/**
  - "**/.allison1875.yml"
  - "**/forms.yml"
alwaysApply: false
---

# Developer Workflows

## Build & Install

```bash
# Clone and install all modules into the local Maven repo
git clone git@github.com:spldeolin/allison1875.git
mvn -f allison1875/pom.xml install -DskipTests

# Compile only
mvn compile

# Full build + tests + JaCoCo report (single-module + aggregate)
mvn verify
```

The `allison1875-cli` module produces a fat jar (`allison1875-cli/target/allison1875.jar`)
via `maven-shade-plugin` with `Entrypoint` as the manifest `Main-Class`.

## Installing the CLI Globally

```bash
# Build, copy fat jar to /usr/local/lib/allison1875/, install /usr/local/bin/allison1875 wrapper
./install-cli.sh

# Remove the global install
./uninstall-cli.sh
```

Wrapper script just does:

```bash
exec java -jar /usr/local/lib/allison1875/allison1875-cli.jar "$@"
```

## Running a Tool Against a Target Project

After `./install-cli.sh`, run from any directory (typically the target Spring Boot project root):

```bash
allison1875 --tool=doc-analyzer          --domain=<name> --config=./.allison1875.yml
allison1875 --tool=handler-transformer   --domain=<name> --config=./.allison1875.yml
allison1875 --tool=persistence-generator --domain=<name> --config=./.allison1875.yml
allison1875 --tool=query-transformer     --domain=<name> --config=./.allison1875.yml
allison1875 --tool=star-transformer      --domain=<name> --config=./.allison1875.yml
allison1875 --tool=form-generator        --domain=<name> --config=./.allison1875.yml
```

`--domain=` may be omitted if `.allison1875.yml` defines exactly one entry under `domains:`.

Without installing globally:

```bash
java -jar allison1875-cli/target/allison1875.jar --tool=doc-analyzer --config=./.allison1875.yml
```

## `.allison1875.yml` Sketch

```yaml
author: Deolin
enableNoModifyAnnounce: true
enableJavaxMoveToJakarta: false

domains:
  - name: order
    controllerModule: /abs/path/to/order-api
    controllerPackage: com.example.order.controller
    dtoModule:        /abs/path/to/order-api
    reqDTOPackage:    com.example.order.dto.req
    respDTOPackage:   com.example.order.dto.resp
    enumModule:       /abs/path/to/order-domain
    enumPackage:      com.example.order.enums
    serviceModule:    /abs/path/to/order-service
    servicePackage:   com.example.order.service
    serviceImplModule: /abs/path/to/order-service
    serviceImplPackage: com.example.order.service.impl
    persistenceModule: /abs/path/to/order-persistence
    mapperPackage:    com.example.order.mapper
    entityPackage:    com.example.order.entity
    designPackage:    com.example.order.design
    paramDTOPackage:  com.example.order.mapper.param
    recordDTOPackage: com.example.order.mapper.record
    wholeDTOPackage:  com.example.order.dto.whole
    mapperXmlDirs:
      - src/main/resources/mapper

# tool-specific blocks (codeSnippet, flushTo, dependencyDirsOrJavaFilePath, etc.) follow
```

See `allison1875-yml-methodology.md` at the repo root for the canonical methodology of writing
`.allison1875.yml` and `forms.yml`.

## Running Unit / Integration Tests

```bash
# Compile + run all JUnit 5 tests in allison1875-cli (covers all tool ITs in-process)
mvn test -pl allison1875-cli -am

# Same + JaCoCo aggregate report
mvn verify -pl allison1875-cli -am

# A single IT test class
mvn test -pl allison1875-cli -am -Dtest=BasicMarkdownItTest

# A single IT test method
mvn test -pl allison1875-cli -am -Dtest=BasicMarkdownItTest#shouldGenerateMarkdown
```

Older `main()`-style demo classes in some modules (`common/src/test`, etc.) still exist and
are run from the IDE; new tests should always use JUnit 5.

## Scaffolding: New Tool Module

To create a new Allison 1875 tool, the steps are:

```java
// 1. Create the Module (XxxModule.java) — single-arg Config constructor is REQUIRED
@ToString
public class XxxModule extends Allison1875Module {
    private final Config config;
    public XxxModule(Config config) { this.config = config; }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return Xxx.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
    }
}

// 2. Create the MainService (Xxx.java)
@Singleton
@Slf4j
public class Xxx implements Allison1875MainService {
    @Inject private Config config;
    // @Inject other services...

    @Override
    public void process() {
        // DomainContext.get() gives DomainConfig (already source-root-resolved)
        for (CompilationUnit cu : AstForestContext.get()) {
            // detection → analysis → generation → write
        }
        log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
    }
}
```

```java
// 3. Wire the new tool into the CLI dispatcher
//    (a) Add a String field on Config (e.g. xxxModule = "com.spldeolin.allison1875.xxx.XxxModule")
//        with a default value pointing at the new Module FQN.
//    (b) Add an entry to ToolEnum:
//          XXX("xxx", Config::getXxxModule, false)
//    (c) Add the new Maven module to allison1875-cli's <dependencies> so Entrypoint can
//        Class.forName the new Module.
//    (d) (Optional) Add an IT base class XxxItBaseTest under
//        allison1875-cli/src/test/java/.../it/xxx/, mirroring DocAnalyzerItBaseTest.
```

## Scaffolding: New Service Interface + Implementation

```java
// Interface (in service/ package)
@ImplementedBy(XxxServiceImpl.class)
public interface XxxService {
    ReturnType methodName(ArgType arg);
}

// Implementation (in service/impl/ package)
@Singleton
@Slf4j
public class XxxServiceImpl implements XxxService {
    @Inject private Config config;

    @Override
    public ReturnType methodName(ArgType arg) {
        // implementation
    }
}
```

## Integration Testing in `allison1875-cli` (JUnit 5, in-process)

**Overview:**
All IT cases live as JUnit 5 tests in `allison1875-cli/src/test/java/.../it/<tool>/`. Each test
class extends a tool-specific base (`<Tool>ItBaseTest`) which:

1. Recursively copies test resources from
   `allison1875-cli/src/test/resources/it/<tool>/<caseName>/` to
   `allison1875-cli/target/it/<caseName>/` (the per-test `basedir`).
2. Reads the case's `.allison1875.yml` and rewrites every relative path
   (`*Module`, `markdownDir`, `dslDir`, `dependencyDirs`, `mapperXmlDirs`, …) into an absolute
   path under `basedir`, then writes the YAML back.
3. Builds CLI args (`--tool=...`, `--config=...`, optional `--domain=...`) and calls
   `Entrypoint.main(args)` in the same JVM.
4. Saves and restores the thread context ClassLoader because `DefaultAstForest` swaps it for an
   IT-scoped `URLClassLoader` and the post-test assertions need the original to load
   classpath resources (`allison1875-git.properties` etc.).
5. The `@Test` method then asserts on files under `basedir`.

**Test source layout:**

```
allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/
├── docanalyzer/
│   ├── DocAnalyzerItBaseTest.java
│   ├── BasicMarkdownItTest.java
│   └── ...
├── handlertransformer/
│   ├── HandlerTransformerItBaseTest.java
│   └── ...
├── persistencegenerator/
│   └── PersistenceGeneratorItBaseTest.java
├── querytransformer/
│   └── ...
└── formgenerator/
    └── ...

allison1875-cli/src/test/resources/it/<tool>/<caseName>/
├── .allison1875.yml          # Tool config; *Module fields are RELATIVE to this case dir
├── forms.yml                 # (form-generator only)
├── sql/                      # (form-generator/persistence-generator only — DDL fixtures)
└── src/main/java/...         # Fake Java sources the tool will analyze/transform
```

**Coverage (JaCoCo) — always on, no profile required:**

- `jacoco-maven-plugin 0.8.12` is bound directly under `<build><plugins>` in
  `allison1875-cli/pom.xml`.
- `prepare-agent` (initialize phase) injects the JaCoCo agent into surefire's `argLine`.
- `report` (test phase) → `target/site/jacoco/index.html` (single-module report).
- `report-aggregate` (verify phase) → `target/site/jacoco-aggregate/index.html`
  (covers `common`, `doc-analyzer`, `handler-transformer`, `persistence-generator`,
  `query-transformer`, `star-transformer`, `form-generator` because they are all Maven
  dependencies of `allison1875-cli`).
- Because every IT case calls `Entrypoint.main(...)` in the same JVM as the JUnit runner, all
  tool code paths are covered without subprocess instrumentation.

**Output locations:**

```
allison1875-cli/target/
├── jacoco.exec                          # Raw coverage data
├── site/
│   ├── jacoco/                          # Single-module report (allison1875-cli only)
│   └── jacoco-aggregate/                # Cross-module aggregate report
│       ├── index.html                   # Open this for the full HTML report
│       ├── jacoco.xml
│       └── jacoco.csv
└── it/<caseName>/                       # Per-case scratch dir (basedir for assertions)
```

**Migration guides:**
The four `allison1875-cli/<tool>-it-migration-guide.md` files document how IT cases were
ported into this layout. They are the authoritative reference when adding new IT cases — in
particular, they describe how the `DomainConfig.*Module` fields must be set up.

**Key rules for IT development:**

- `*Module` paths in `.allison1875.yml` MUST be relative inside the resource case dir —
  the base class converts them to absolute. Never check in absolute paths.
- Always restore the thread context ClassLoader after `Entrypoint.main` (the base class does this
  for you; do not call `Entrypoint.main` directly without the wrapper).
- `target/it/<caseName>/` is regenerated on every test run; never reference it from another test.
- New ITs must extend the appropriate `<Tool>ItBaseTest`, not invoke `Entrypoint` ad hoc.
- Run `mvn install -DskipTests` from the repo root after touching tool source code, otherwise
  the cli-side IT will reflect on stale Module class bytes in your local Maven repo.
