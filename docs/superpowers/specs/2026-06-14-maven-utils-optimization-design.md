# MavenUtils Optimization: Classloader-Isolated Maven Embedder

## Problem

`MavenUtils.compile()` and `executeBuildClasspath()` use `sh -c "mvn ..."` to fork Maven processes. The JVM startup overhead (~2-3s per invocation) dominates execution time, even with `-T 1C`.

## Solution

Load Maven's own JARs into an isolated `URLClassLoader` and invoke `MavenCli.doMain()` via reflection. This eliminates JVM fork overhead while avoiding the Guice 5.x vs 7.0.0 classpath conflict through classloader isolation.

## Architecture

### EmbeddedMavenInvoker (new private static inner helper in MavenUtils)

Responsibilities:
1. **Locate Maven installation** — resolve in order: (a) `MAVEN_HOME` env var, (b) `M2_HOME` env var, (c) resolve `mvn` binary on PATH, follow symlinks to real path, navigate up to parent directory (e.g., `/opt/homebrew/Cellar/maven/3.9.12/libexec/bin/mvn` → `libexec/`). Validate by checking `lib/` subdirectory contains JARs.
2. **Build isolated ClassLoader** — scan `MAVEN_HOME/boot/*.jar` + `MAVEN_HOME/lib/*.jar`, create `new URLClassLoader(jars, null)` (null parent = full isolation from app classloader)
3. **Invoke MavenCli** — reflectively load `org.apache.maven.cli.MavenCli`, call `doMain(String[], String, PrintStream, PrintStream)`
4. **Cache ClassLoader** — static lazy singleton, reused across all invocations in the same JVM

### Method Changes

**`compile(File mavenModuleDir, String javaHome)`:**
- Build args array: `["compile", "-q"]` or `["compile", "-pl", relativePath, "-am", "-q"]`
- If javaHome specified: add `-Dmaven.compiler.fork=true`, `-Dmaven.compiler.executable=.../javac`
- Set system property `maven.multiModuleProjectDirectory` to topLevelDir
- Invoke via EmbeddedMavenInvoker
- Capture stdout/stderr via `ByteArrayOutputStream`-backed `PrintStream`

**`executeBuildClasspath(File mavenModuleDir, String javaHome)`:**
- Build args array: `["compile", "dependency:build-classpath", "-DincludeScope=compile", "-Dmdep.outputFile=<tmpFile>", "-q"]` (plus `-pl`/`-am` for multi-module)
- Same invocation mechanism
- Read classpath from temp file (unchanged logic)

### Thread Safety

`MavenCli` is not thread-safe. Protect the invocation entry point with a `ReentrantLock`. Current usage is sequential, so no performance impact.

### Fallback

If Maven installation cannot be located (missing MAVEN_HOME, `mvn` not on PATH), fall back to the current ProcessBuilder-based approach with a warning log. This ensures CI/container environments without a standard Maven installation still work.

### Output Handling

Replace process stdout/stderr reading with `ByteArrayOutputStream`-wrapped `PrintStream` instances passed to `doMain()`. On failure (exit code != 0), log captured output and throw `Allison1875Exception` (same behavior as current).

### What Does NOT Change

- Public API signatures of `compile()`, `buildClassLoader()`, `consumeDependencySourceCus()`
- `findTopLevelProjectDir()`, `calculateRelativePath()`, `parseClasspathToUrls()`
- `executeSourceJarInstall()`, `resolveLocalRepoPath()`, `resolveDependencyGavs()` (not in scope)
- Temp file approach for classpath output

## Unit Tests

### Test Framework

Add JUnit 5 (`junit-jupiter:5.10.2`) as test-scope dependency to `common/pom.xml`.

### Test Resources

Create a minimal Maven project under `common/src/test/resources/maven-test-project/`:
- `pom.xml` — minimal POM with one compile dependency (e.g., `commons-lang3`)
- `src/main/java/com/example/Hello.java` — single Java file that compiles successfully

Create a second project for multi-module testing:
- `multi-module-project/pom.xml` — parent POM with `<modules>`
- `multi-module-project/module-a/pom.xml` — child module A (no deps)
- `multi-module-project/module-b/pom.xml` — child module B depends on A
- `multi-module-project/module-b/src/main/java/com/example/UsesA.java`

### Test Class: `MavenUtilsTest`

Located at `common/src/test/java/com/spldeolin/allison1875/common/util/MavenUtilsTest.java`

**Test cases:**

1. `compile_singleModule_success` — compile the single-module test project, verify `target/classes` contains compiled .class file
2. `compile_multiModule_success` — compile module-b (which depends on module-a), verify both modules compile
3. `compile_invalidDir_throwsException` — pass non-existent directory, expect `Allison1875Exception`
4. `compile_noPomDir_throwsException` — pass directory without pom.xml, expect `Allison1875Exception`
5. `buildClassLoader_singleModule_returnsWorkingClassLoader` — build classloader, verify it can load classes from the compile dependency (e.g., `org.apache.commons.lang3.StringUtils`)
6. `buildClassLoader_includesTargetClasses` — verify the returned classloader can load the project's own compiled class
7. `buildClassLoader_invalidDir_throwsException` — null/non-existent dir throws
8. `findTopLevelProjectDir_multiModule_findsRoot` — verify it navigates up to the aggregator POM
9. `findTopLevelProjectDir_singleModule_returnsSelf` — single module returns itself

### Test for EmbeddedMavenInvoker (internal)

Since EmbeddedMavenInvoker is private, test it indirectly through `compile()` and `buildClassLoader()`. Additionally, one test to verify the fallback:

10. `compile_whenMavenHomeNotFound_fallsBackToProcess` — temporarily unset MAVEN_HOME and verify compile still works (via process fallback)

### Test Lifecycle

- `@BeforeAll`: Copy test resources to a temp directory (to avoid polluting source tree with `target/` directories)
- `@AfterAll`: Clean up temp directory
- Tests that invoke Maven are inherently integration tests (require Maven installed). Tag with `@Tag("integration")`. Configure `maven-surefire-plugin` to exclude this tag by default; run with `-DincludeTags=integration` to include them.
