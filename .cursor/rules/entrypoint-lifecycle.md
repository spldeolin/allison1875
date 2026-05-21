---
description: CLI entry point, argument parsing, and per-invocation lifecycle of Allison1875.letsGo.
globs:
  - allison1875-cli/**
  - common/src/main/java/**/Allison1875.java
  - common/src/main/java/**/Entrypoint.java
alwaysApply: false
---

# Application Entry Points & Main Loop

## CLI Entry (`allison1875-cli/.../Entrypoint.java`)

```bash
allison1875 --tool=<toolName> [--domain=<domainName>] --config=<path/to/.allison1875.yml>
```

Internally:

```
Allison1875.hello()                                  → print banner + version
Allison1875.letsGo(ToolEnum tool, Config config, String domainName)
    → resolveDomain → resolveSourceRoots
    → DomainContext.set(domainConfig)
    → buildModule(tool, config)   (composite for FORM_GENERATOR)
    → Guice injector with [toolModule, ValidationModule]
    → injector.getInstance(MainService).process()
```

## Entrypoint CLI Argument Format

Parsed by `Entrypoint.parseArgs`, prefix-based, order-agnostic:

- `--tool=<toolName>` — required. Resolved via `ToolEnum.of(...)`. Valid names:
  `doc-analyzer`, `handler-transformer`, `persistence-generator`, `query-transformer`,
  `star-transformer`, `form-generator`.
- `--config=<path>` — required. Path to `.allison1875.yml`, deserialized with SnakeYAML
  into `Config`.
- `--domain=<domainName>` — optional when the YAML defines exactly one `domains[]` entry;
  required when multiple are defined. Matches `DomainConfig.name`.

## Lifecycle (per CLI invocation)

1. `Entrypoint.main` parses args → resolves `ToolEnum`, loads `Config` from YAML.
2. `Allison1875.letsGo` resolves the target `DomainConfig` from `config.domains`.
3. Each `*Module` field on the `DomainConfig` (controllerModule, dtoModule, enumModule,
   serviceModule, serviceImplModule, persistenceModule) is converted into a `*SourceRoot`
   absolute `Path` (via `<module>/src/main/java`); `mapperXmlDirs` is resolved against
   `persistenceModule`.
4. `DomainContext.set(domainConfig)` (ThreadLocal) — every downstream service reads the active
   domain via `DomainContext.get()`.
5. `buildModule`:
    - Simple tools → reflectively `new <Config.<tool>Module>(config)`.
    - Composite tool (`FORM_GENERATOR`) → `Modules.override` chains
      `persistence → handler → doc → query → form` sub-modules into one `Allison1875Module`.
6. Guice injector created with `[allison1875Module, ValidationModule]`. `CreationException`
   carrying an `Allison1875Exception` cause is unwrapped and re-thrown.
7. `injector.getInstance(allison1875Module.declareMainService()).process()` runs the tool's
   main loop. Any throwable becomes `Allison1875Exception`.

## Tool Main Loop

Each tool implements `Allison1875MainService.process()` — that is the "game loop" equivalent.
Inside `process()`, the tool typically iterates `AstForestContext.get()` (an
`Iterable<CompilationUnit>` set up per-tool from the `DomainContext`-derived source roots) and
performs detection → analysis → code generation → file write. `DomainContext.get()` provides the
per-domain package/source-root metadata needed at every step.
