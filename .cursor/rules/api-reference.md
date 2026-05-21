---
description: Quick-reference for core utility functions, constants, enums, and common entity/object access patterns.
globs:
  - common/src/main/java/**/util/**
  - common/src/main/java/**/constant/**
  - common/src/main/java/**/enums/**
  - common/src/main/java/**/context/**
alwaysApply: false
---

# Rapid Reference & Context Map

## Core Utility Functions (with exact signatures)

```java
// === JsonUtils (com.spldeolin.allison1875.common.util.JsonUtils) ===
public static String toJson(Object object)

public static String toJsonPrettily(Object object)

public static <T> T toObject(String json, Class<T> clazz)

public static <T> List<T> toListOfObject(String json, Class<T> clazz)

public static JsonNode toTree(String json)

public static ObjectMapper createObjectMapper()

// === CompilationUnitUtils (com.spldeolin.allison1875.common.util.CompilationUnitUtils) ===
public static CompilationUnit parseJava(File javaFile)

public static Path getCuAbsolutePath(CompilationUnit cu)

public static Optional<CompilationUnit> tryFindCu(Path sourceRoot, String primaryTypeQualifier)

public static void writeJava(CompilationUnit cu)

public static void writeJava(CompilationUnit cu, boolean lexicalPreserving)

public static CompilationUnit newBaseCurrentAstForest()

// === MoreStringUtils (com.spldeolin.allison1875.common.util.MoreStringUtils) ===
public static String toUpperCamel(String string)

public static String toLowerCamel(String string)

public static List<String> splitLineByLine(String string)

public static String replaceLast(String from, String target, String replacement)

public static String camelToSnakeCase(String camelStr)

// === CollectionUtils (com.spldeolin.allison1875.common.util.CollectionUtils) ===
public static boolean isEmpty(Collection<?> collection)

public static boolean isNotEmpty(Collection<?> collection)

// === HashingUtils (com.spldeolin.allison1875.common.util.HashingUtils) ===
public static String hashString(String string)

public static String hashTypeDeclaration(TypeDeclaration<?> typeDeclaration)

// === ValidUtils (com.spldeolin.allison1875.common.util.ValidUtils) ===
public static List<InvalidDTO> valid(Object object)

// === JavadocUtils (com.spldeolin.allison1875.common.util.JavadocUtils) ===
public static Javadoc setJavadoc(NodeWithJavadoc<?> node, String description, String author)

public static String getDescription(NodeWithJavadoc<?> node)

public static List<String> getDescriptionAsLines(NodeWithJavadoc<?> node)

// === FileSnapshotUtils (com.spldeolin.allison1875.common.util.FileSnapshotUtils) ===
public static FileSystemSnapshot createSnapshot(File basePath)

public static void rollback(FileSystemSnapshot snapshot)
```

## Key Constants (`BaseConstant`)

```java
// com.spldeolin.allison1875.common.constant.BaseConstant
String SINGLE_INDENT = "    ";                        // 4 spaces

String DOUBLE_INDENT = Strings.repeat(SINGLE_INDENT, 2);

String TREBLE_INDENT = Strings.repeat(SINGLE_INDENT, 3);

String NEW_LINE = System.lineSeparator();

String JAVA_DOC_NEW_LINE = System.lineSeparator() + "<p>";

String NEW_LINE_FOR_MATCHING = "[\\r\\n]+";

String LOT_NO_ANNOUNCE_PREFIXION = "Allison 1875 Lot No: ";

String NO_MODIFY_ANNOUNCE = "Any modifications may be overwritten by future code generations.";

String[] JAVA_EXTENSIONS = new String[]{"java"};

String REMEMBER_REFORMAT_CODE_ANNOUNCE = "# REMEMBER REFORMAT CODE #";
```

## Key Enums

```java
// FileExistenceResolutionEnum: OVERWRITE, RENAME
// FlushToEnum:                 MARKDOWN, YAPI, SHOWDOC, DSL
// PageParamStyleEnum:          PAGE_NO_PAGE_SIZE, OFFSET_LIMIT
// ToolEnum:                    DOC_ANALYZER, HANDLER_TRANSFORMER, PERSISTENCE_GENERATOR,
//                              QUERY_TRANSFORMER, STAR_TRANSFORMER, FORM_GENERATOR (composite)
```

`ToolEnum` carries `(toolName, moduleClassNameGetter, composite)`. `composite=true` triggers
`Allison1875.buildCompositeModule` which `Modules.override`-chains sub-tool modules.

## Common Entity/Object Access

| What                   | How to access                                                                      |
|------------------------|------------------------------------------------------------------------------------|
| **Current Domain**     | `DomainContext.get()` → `DomainConfig` (ThreadLocal, set by `Allison1875.letsGo`)  |
| **Domain source root** | e.g. `DomainContext.get().getControllerSourceRoot()` (one per layer)               |
| **Domain package**     | e.g. `DomainContext.get().getReqDTOPackage()`                                      |
| **Mapper XML dirs**    | `DomainContext.get().getMapperXmlDirs()` — already absolute by `letsGo`            |
| **Current AstForest**  | `AstForestContext.get()` (ThreadLocal, populated per-tool from the `DomainConfig`) |
| **Find a CU by FQN**   | `AstForestContext.get().tryFindCu(qualifiedName)` → `Optional<CompilationUnit>`    |
| **Iterate all CUs**    | `for (CompilationUnit cu : AstForestContext.get()) { ... }`                        |
| **Config**             | Inject `Config config` via `@Inject` in Guice-managed classes                      |
| **Services**           | Inject via `@Inject private XxxService xxxService;`                                |
