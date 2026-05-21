---
description: Detailed code style rules — package structure, file headers, Lombok annotations, comment conventions, utility class pattern, and import ordering.
globs:
  - "**/*.java"
alwaysApply: false
---

# Code Style & Consistency (Details)

## Package Structure per Module

Each tool module follows:

```
com.spldeolin.allison1875.<toolname>/
├── <ToolName>.java                    # implements Allison1875MainService (the main class)
├── <ToolName>Module.java              # extends Allison1875Module (Guice bindings)
├── dto/                               # Data transfer objects for internal use
├── enums/                             # Module-specific enums
├── service/                           # Service interfaces
│   └── impl/                          # Service implementations
└── util/                              # Module-specific utilities (if any)
```

`allison1875-cli` is the only module without a `MainService`; it owns just `Entrypoint` plus the
`it/<toolname>/` JUnit 5 IT trees.

## Header/Footer Standards

- **File header:** Every Java file MUST have a Javadoc with `@author` tag and date:
  ```java
  /**
   * @author Deolin 2026-04-28
   */
  ```
  Date format: `yyyy-MM-dd`. Some older files omit the description line above `@author`.

- **No file footer** is required.

## Lombok Usage

- **DTO classes:** `@Data`, `@Accessors(chain = true)`, `@FieldDefaults(level = AccessLevel.PRIVATE)`
- **Config classes (`Config`, `DomainConfig`):** `@Data`, `@FieldDefaults(level = AccessLevel.PRIVATE)`
- **Enum classes:** `@Getter`, `@AllArgsConstructor`
- **Service impls & main classes:** `@Slf4j`
- **Guice module classes:** `@Slf4j`, `@ToString`

## Type Hinting & Comments

- **Chinese comments** are used extensively for inline and Javadoc descriptions.
- Javadoc is expected on all public interfaces and important methods.
- Validation annotations (`@NotNull`, `@NotEmpty`, `@NotBlank`, `@Valid`) serve as type contracts on
  `Config`, `DomainConfig`, and DTO fields.
- **Always use `jakarta.validation` annotations (NOT `javax.validation`) inside the tool itself.**
  The tool may *generate* `javax.validation` or `jakarta.validation` annotations into target
  projects depending on `Config.enableJavaxMoveToJakarta`, but the tool's own source code is
  Jakarta-only.

## Utility Class Pattern

All utility classes MUST follow:

```java
public class XxxUtils {
    private XxxUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }
    // static methods only
}
```

## Import Ordering

Follow the observed order:

1. `java.*`
2. `javax.*` (rare; mostly absent now)
3. `jakarta.*`
4. Third-party (`org.*`, `com.*`)
5. Project-internal (`com.spldeolin.allison1875.*`)
6. Lombok (`lombok.*`) — always last

No wildcard imports. Every import is explicit.
