---
description: Architecture patterns — Guice DI, AST processing pipeline, error handling, file snapshot, config validation, DTO conventions, query-transformer DSL rules, and composite tool (form-generator).
globs:
  - common/src/main/java/**
  - handler-transformer/**
  - persistence-generator/**
  - query-transformer/**
  - star-transformer/**
  - doc-analyzer/**
  - form-generator/**
alwaysApply: false
---

# Architecture-Specific Patterns

## Dependency Injection (Guice, NOT Spring)

- The tool itself uses **Google Guice**, not Spring.
- Every tool module extends `Allison1875Module` and implements `declareMainService()`.
- Tool modules are constructed reflectively with a **single `Config` constructor argument**;
  `Class.forName(<Config.<tool>Module>).getConstructor(Config.class).newInstance(config)`.
- Service interfaces use `@ImplementedBy(XxxServiceImpl.class)` for default bindings.
- Custom bindings go in `configure()`:
  ```java
  @Override
  protected void configure() {
      bind(Config.class).toInstance(config);
      bind(DataModelService.class).toInstance(new DataModelServiceImpl());
  }
  ```
- Use `@Inject` for field injection, `@Singleton` on implementation classes.
- The `ValidationModule` auto-registers parameter validation interceptors for all Guice-managed
  methods (jakarta.validation backed by Hibernate Validator).

## AST Processing Pipeline

Standard processing pattern in `process()`:

```java
@Override
public void process() {
    // DomainContext.get() gives the active DomainConfig (source roots + packages)
    for (CompilationUnit cu : AstForestContext.get()) {
        // 1. Detect target AST nodes
        // 2. Analyze / extract information
        // 3. Generate new code (DTOs, services, etc.)
        // 4. Modify existing CU if needed
    }
    // 5. Extract qualified types to imports: importExprService.extractQualifiedTypeToImport(cu)
    // 6. Write modified CUs: CompilationUnitUtils.writeJava(cu)
    // 7. Log REMEMBER_REFORMAT_CODE_ANNOUNCE
}
```

**CRITICAL:** Always call `importExprService.extractQualifiedTypeToImport(cu)` BEFORE
`CompilationUnitUtils.writeJava(cu)` — the former converts fully qualified type names in the AST
into proper import statements.

## Error Handling

- Use `Allison1875Exception` (extends `RuntimeException`) for all domain errors.
- Constructors: `(String message)`, `(Throwable cause)`, `(String message, Throwable cause)`.
- In `Allison1875.letsGo()`, `CreationException` whose cause is an `Allison1875Exception` is
  unwrapped and re-thrown; otherwise it propagates.
- Any `Throwable` from `MainService.process()` is logged at `error` and wrapped in
  `Allison1875Exception`.
- IO errors: wrap `IOException` in `UncheckedIOException` or `Allison1875Exception`.
- Entrypoint-level argument errors: throw `Allison1875Exception` with a Chinese user-facing message.

## File Snapshot & Rollback

`FileSnapshotUtils` still exists for tools that need point-in-time snapshots of generated
artifacts. With the CLI architecture there is no automatic project-wide rollback wrapper —
tools that want rollback must explicitly create and restore a snapshot.

```java
FileSystemSnapshot snapshot = FileSnapshotUtils.createSnapshot(basedir);
try { /* mutate */ }
catch (Throwable e) { FileSnapshotUtils.rollback(snapshot); throw e; }
```

## Config Validation

`Config` uses `@ConfigValid` (custom annotation, validated by `ConfigValidator`) plus Hibernate
Validator annotations from `jakarta.validation.constraints.*`.
`ValidationModule` installs `ValidSingletonListener` and `ValidMethodArgsInterceptor` for
automatic validation of `@Singleton` beans at creation time and method args at invocation.
`DomainConfig` is validated via `@Valid` cascade from `Config.domains`.

## DTO Conventions

DTOs follow a strict pattern:

```java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxArg {          // input args
    @NotNull Type field;       // jakarta.validation.constraints.NotNull
}

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxRetval {       // return values
    Type field;
}

// or for pure data:
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxDTO { ... }
```

Naming: `*Arg` for inputs, `*Retval` for outputs, `*DTO` for data transfer.

## Query-Transformer DSL Assignment Rules

The `query-transformer` operates on Design chain DSL written in Service classes. The DSL has
a critical compile-time vs transform-time type distinction:

**Compile-time type:** Design chain methods (e.g. `.list()`) always return `List<TXxxEntity>`
regardless of selected properties. This is because the Design class is a compile-time stub.

**Transform-time type:** The query-transformer resolves the actual return type based on selected
properties during AST transformation.

**Rules for writing DSL in IT cases and target projects:**

| Scenario                        | DSL Code                                                            | Compile-time Type                | Transform Result                  |
|---------------------------------|---------------------------------------------------------------------|----------------------------------|-----------------------------------|
| No assignment (single property) | `TOrderDesign.select("xxx").orderNo.list();`                        | `List<TOrderEntity>` (discarded) | `List<String>`                    |
| No assignment (multi property)  | `TOrderDesign.select("xxx").orderNo.userId.list();`                 | `List<TOrderEntity>` (discarded) | `List<XxxRecord>` (generated DTO) |
| With assignment                 | `List<TOrderEntity> r = TOrderDesign.select("xxx").orderNo.list();` | `List<TOrderEntity>` ✓           | `List<TOrderEntity>` (kept as-is) |
| **INVALID**                     | `List<String> r = TOrderDesign.select("xxx").orderNo.list();`       | Compilation error                | —                                 |

**Key takeaway:** If the developer wants the transformer to resolve a non-Entity return type
(e.g. `List<String>` for single property, or `List<Record>` for multi properties), the DSL
statement MUST be written **without assignment**. Assigning to a variable forces the compile-time
type `List<TXxxEntity>`, which the transformer will preserve.

## Composite Tool — `form-generator`

`form-generator` is the only `composite` tool in `ToolEnum`. `Allison1875.buildCompositeModule`
loads the four sub-tool modules referenced by `Config.{persistenceGenerator,handlerTransformer,
docAnalyzer,queryTransformer}Module`, then chains them with `Modules.override(...)` in the order:
`persistence → handler → doc → query → form`. The final `mainService` comes from
`FormGeneratorModule`. Sub-tool bindings can be selectively overridden by `FormGeneratorModule`
because `Modules.override` lets later modules win. Never reorder the chain or skip sub-tools —
`form-generator` relies on the full stack to be wired up.
