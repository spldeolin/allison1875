# MavenUtils Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace shell-based Maven process invocation with classloader-isolated in-process Maven Embedder calls, eliminating JVM fork overhead.

**Architecture:** Load Maven installation JARs into an isolated URLClassLoader (null parent), reflectively invoke MavenCli.doMain(). Cache the classloader as a static singleton. Fall back to ProcessBuilder if Maven home cannot be located.

**Tech Stack:** Java 21, Maven Resolver via reflection, JUnit 5 for tests

---

## File Structure

| File | Responsibility |
|------|---------------|
| `common/src/main/java/.../util/MavenUtils.java` | Modify: refactor compile() and executeBuildClasspath() to use embedded invoker |
| `common/pom.xml` | Modify: add JUnit 5 test dependency |
| `common/src/test/java/.../util/MavenUtilsTest.java` | Create: integration tests |
| `common/src/test/resources/maven-test-project/pom.xml` | Create: single-module test project POM |
| `common/src/test/resources/maven-test-project/src/main/java/com/example/Hello.java` | Create: compilable source |
| `common/src/test/resources/multi-module-project/pom.xml` | Create: parent aggregator POM |
| `common/src/test/resources/multi-module-project/module-a/pom.xml` | Create: child module A |
| `common/src/test/resources/multi-module-project/module-a/src/main/java/com/example/ModuleA.java` | Create: module A source |
| `common/src/test/resources/multi-module-project/module-b/pom.xml` | Create: child module B (depends on A) |
| `common/src/test/resources/multi-module-project/module-b/src/main/java/com/example/UsesA.java` | Create: module B source |

---

### Task 1: Add JUnit 5 test dependency to common module

**Files:**
- Modify: `common/pom.xml`

- [ ] **Step 1: Add junit-jupiter dependency**

Add to `common/pom.xml` inside `<dependencies>`:

```xml
<!-- test -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Verify dependency resolves**

Run: `mvn dependency:resolve -pl common -DincludeScope=test -q`
Expected: exits 0, no errors

- [ ] **Step 3: Commit**

```bash
git add common/pom.xml
git commit -m "build: add JUnit 5 test dependency to common module"
```

---

### Task 2: Implement EmbeddedMavenInvoker in MavenUtils

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java`

- [ ] **Step 1: Add required imports**

Add these imports to MavenUtils.java:

```java
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;
```

- [ ] **Step 2: Add EmbeddedMavenInvoker static fields and lock**

Add these fields after the private constructor:

```java
private static final ReentrantLock MAVEN_INVOKE_LOCK = new ReentrantLock();
private static volatile ClassLoader mavenClassLoader;
private static volatile Method mavenDoMainMethod;
private static volatile Object mavenCliInstance;
private static volatile boolean embeddedMavenAvailable = true;
```

- [ ] **Step 3: Implement resolveMavenHome()**

Add this method:

```java
private static File resolveMavenHome() {
    // (a) MAVEN_HOME env
    String mavenHome = System.getenv("MAVEN_HOME");
    if (mavenHome != null) {
        File dir = new File(mavenHome);
        if (new File(dir, "lib").isDirectory()) {
            return dir;
        }
    }
    // (b) M2_HOME env
    String m2Home = System.getenv("M2_HOME");
    if (m2Home != null) {
        File dir = new File(m2Home);
        if (new File(dir, "lib").isDirectory()) {
            return dir;
        }
    }
    // (c) resolve from 'mvn' on PATH via symlink
    try {
        ProcessBuilder pb = new ProcessBuilder("which", "mvn");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String mvnPath;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            mvnPath = reader.readLine();
        }
        process.waitFor();
        if (mvnPath != null && !mvnPath.isEmpty()) {
            // follow symlinks to real path
            Path realPath = Path.of(mvnPath).toRealPath();
            // mvn binary is at <maven-home>/bin/mvn
            File binDir = realPath.getParent().toFile();
            File home = binDir.getParentFile();
            if (home != null && new File(home, "lib").isDirectory()) {
                return home;
            }
        }
    } catch (Exception e) {
        log.debug("failed to resolve maven home from PATH", e);
    }
    return null;
}
```

- [ ] **Step 4: Implement initEmbeddedMaven()**

Add this method:

```java
private static void initEmbeddedMaven() {
    if (mavenDoMainMethod != null) {
        return;
    }
    synchronized (MavenUtils.class) {
        if (mavenDoMainMethod != null) {
            return;
        }
        File mavenHome = resolveMavenHome();
        if (mavenHome == null) {
            embeddedMavenAvailable = false;
            log.warn("cannot locate Maven installation, will fall back to process-based invocation");
            return;
        }
        try {
            List<URL> jars = new ArrayList<>();
            // boot directory (plexus-classworlds)
            File bootDir = new File(mavenHome, "boot");
            if (bootDir.isDirectory()) {
                File[] bootJars = bootDir.listFiles((d, n) -> n.endsWith(".jar"));
                if (bootJars != null) {
                    for (File jar : bootJars) {
                        jars.add(jar.toURI().toURL());
                    }
                }
            }
            // lib directory (all Maven jars)
            File libDir = new File(mavenHome, "lib");
            File[] libJars = libDir.listFiles((d, n) -> n.endsWith(".jar"));
            if (libJars != null) {
                for (File jar : libJars) {
                    jars.add(jar.toURI().toURL());
                }
            }
            // also include lib/ext if present
            File extDir = new File(libDir, "ext");
            if (extDir.isDirectory()) {
                File[] extJars = extDir.listFiles((d, n) -> n.endsWith(".jar"));
                if (extJars != null) {
                    for (File jar : extJars) {
                        jars.add(jar.toURI().toURL());
                    }
                }
            }
            if (jars.isEmpty()) {
                embeddedMavenAvailable = false;
                log.warn("no JARs found in Maven installation at {}, will fall back to process-based invocation",
                        mavenHome);
                return;
            }
            mavenClassLoader = new URLClassLoader(jars.toArray(new URL[0]), null);
            Class<?> cliClass = mavenClassLoader.loadClass("org.apache.maven.cli.MavenCli");
            mavenCliInstance = cliClass.getDeclaredConstructor().newInstance();
            mavenDoMainMethod = cliClass.getMethod("doMain", String[].class, String.class,
                    PrintStream.class, PrintStream.class);
            log.info("embedded Maven initialized from: {} ({} JARs loaded)", mavenHome, jars.size());
        } catch (Exception e) {
            embeddedMavenAvailable = false;
            log.warn("failed to initialize embedded Maven, will fall back to process-based invocation", e);
        }
    }
}
```

- [ ] **Step 5: Implement invokeEmbeddedMaven()**

Add this method:

```java
private static int invokeEmbeddedMaven(String[] args, File workingDir) {
    initEmbeddedMaven();
    if (!embeddedMavenAvailable) {
        return -1;
    }
    MAVEN_INVOKE_LOCK.lock();
    try {
        String previousMmpdValue = System.getProperty("maven.multiModuleProjectDirectory");
        System.setProperty("maven.multiModuleProjectDirectory", workingDir.getAbsolutePath());

        ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
        try (PrintStream outStream = new PrintStream(outBaos, true, StandardCharsets.UTF_8);
             PrintStream errStream = new PrintStream(errBaos, true, StandardCharsets.UTF_8)) {

            Thread currentThread = Thread.currentThread();
            ClassLoader originalCl = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(mavenClassLoader);
            try {
                int exitCode = (int) mavenDoMainMethod.invoke(mavenCliInstance, args,
                        workingDir.getAbsolutePath(), outStream, errStream);
                if (exitCode != 0) {
                    String output = outBaos.toString(StandardCharsets.UTF_8)
                            + errBaos.toString(StandardCharsets.UTF_8);
                    log.error("embedded Maven failed with exit code {}, output:\n{}", exitCode, output);
                }
                return exitCode;
            } finally {
                currentThread.setContextClassLoader(originalCl);
            }
        } finally {
            if (previousMmpdValue != null) {
                System.setProperty("maven.multiModuleProjectDirectory", previousMmpdValue);
            } else {
                System.clearProperty("maven.multiModuleProjectDirectory");
            }
        }
    } catch (Exception e) {
        log.error("failed to invoke embedded Maven", e);
        return -1;
    } finally {
        MAVEN_INVOKE_LOCK.unlock();
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java
git commit -m "feat: add EmbeddedMavenInvoker to MavenUtils"
```

---

### Task 3: Refactor compile() to use embedded Maven

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java`

- [ ] **Step 1: Replace compile() method body**

Replace the entire `compile` method (lines 48-94) with:

```java
public static void compile(File mavenModuleDir, String javaHome) {
    if (mavenModuleDir == null) {
        throw new Allison1875Exception("mavenModuleDir must not be null");
    }
    if (!mavenModuleDir.isDirectory()) {
        throw new Allison1875Exception("mavenModuleDir is not a directory: " + mavenModuleDir);
    }

    File topLevelDir = findTopLevelProjectDir(mavenModuleDir);

    List<String> args = new ArrayList<>();
    args.add("compile");
    if (!topLevelDir.equals(mavenModuleDir)) {
        String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
        args.add("-pl");
        args.add(relativePath);
        args.add("-am");
    }
    if (javaHome != null && !javaHome.isEmpty()) {
        args.add("-Dmaven.compiler.fork=true");
        args.add("-Dmaven.compiler.executable=" + javaHome + "/bin/javac");
        log.info("using specified JAVA_HOME: {}", javaHome);
    }
    args.add("-q");

    log.info("executing mvn {} (in {})", String.join(" ", args), topLevelDir);

    int exitCode = invokeEmbeddedMaven(args.toArray(new String[0]), topLevelDir);
    if (exitCode == -1) {
        // fallback to process-based invocation
        compileViaProcess(mavenModuleDir, javaHome, topLevelDir);
        return;
    }
    if (exitCode != 0) {
        throw new Allison1875Exception("mvn compile failed with exit code " + exitCode);
    }
    log.info("mvn compile succeeded for: {}", mavenModuleDir);
}
```

- [ ] **Step 2: Extract old compile logic as fallback method**

Add `compileViaProcess` as a private fallback:

```java
private static void compileViaProcess(File mavenModuleDir, String javaHome, File topLevelDir) {
    StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
    commandLine.append(" compile");
    if (!topLevelDir.equals(mavenModuleDir)) {
        String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
        commandLine.append(" -pl ").append(relativePath);
        commandLine.append(" -am");
    }
    commandLine.append(" -q");

    log.info("(fallback) executing: {} (in {})", commandLine, topLevelDir);

    try {
        ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), topLevelDir);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("mvn compile failed with exit code {}, output:\n{}", exitCode, output);
            throw new Allison1875Exception(
                    "mvn compile failed with exit code " + exitCode + ", output:\n" + output);
        }
        log.info("mvn compile succeeded for: {}", mavenModuleDir);
    } catch (Allison1875Exception e) {
        throw e;
    } catch (Exception e) {
        throw new Allison1875Exception("failed to execute mvn compile", e);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java
git commit -m "refactor: compile() uses embedded Maven with process fallback"
```

---

### Task 4: Refactor executeBuildClasspath() to use embedded Maven

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java`

- [ ] **Step 1: Replace executeBuildClasspath() method body**

Replace the entire `executeBuildClasspath` method (lines 369-433) with:

```java
private static String executeBuildClasspath(File mavenModuleDir, String javaHome) {
    File cpOutputFile;
    try {
        cpOutputFile = File.createTempFile("allison1875-cp-", ".txt");
        cpOutputFile.deleteOnExit();
    } catch (Exception e) {
        throw new Allison1875Exception("failed to create temp file for classpath output", e);
    }

    File topLevelDir = findTopLevelProjectDir(mavenModuleDir);

    List<String> args = new ArrayList<>();
    args.add("compile");
    args.add("dependency:build-classpath");
    args.add("-DincludeScope=compile");
    args.add("-Dmdep.outputFile=" + cpOutputFile.getAbsolutePath());
    if (!topLevelDir.equals(mavenModuleDir)) {
        String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
        args.add("-pl");
        args.add(relativePath);
        args.add("-am");
    }
    if (javaHome != null && !javaHome.isEmpty()) {
        args.add("-Dmaven.compiler.fork=true");
        args.add("-Dmaven.compiler.executable=" + javaHome + "/bin/javac");
    }
    args.add("-q");

    log.info("executing mvn {} (in {})", String.join(" ", args), topLevelDir);

    try {
        int exitCode = invokeEmbeddedMaven(args.toArray(new String[0]), topLevelDir);
        if (exitCode == -1) {
            // fallback to process-based invocation
            return executeBuildClasspathViaProcess(mavenModuleDir, javaHome, topLevelDir, cpOutputFile);
        }
        if (exitCode != 0) {
            throw new Allison1875Exception(
                    "mvn dependency:build-classpath failed with exit code " + exitCode);
        }

        String classpath = Files.readString(cpOutputFile.toPath(), StandardCharsets.UTF_8).trim();
        log.debug("resolved classpath: {}", classpath);

        if (classpath.isEmpty()) {
            log.warn("dependency:build-classpath returned empty classpath for: {}", mavenModuleDir);
        }

        return classpath;
    } catch (Allison1875Exception e) {
        throw e;
    } catch (Exception e) {
        throw new Allison1875Exception("failed to execute mvn dependency:build-classpath", e);
    } finally {
        cpOutputFile.delete();
    }
}
```

- [ ] **Step 2: Extract old executeBuildClasspath logic as fallback**

Add `executeBuildClasspathViaProcess`:

```java
private static String executeBuildClasspathViaProcess(File mavenModuleDir, String javaHome,
        File topLevelDir, File cpOutputFile) {
    StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
    commandLine.append(" compile");
    commandLine.append(" dependency:build-classpath");
    commandLine.append(" -DincludeScope=compile");
    commandLine.append(" -Dmdep.outputFile=").append(cpOutputFile.getAbsolutePath());
    if (!topLevelDir.equals(mavenModuleDir)) {
        String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
        commandLine.append(" -pl ").append(relativePath);
        commandLine.append(" -am");
    }
    commandLine.append(" -q");

    log.info("(fallback) executing: {} (in {})", commandLine, topLevelDir);

    try {
        ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), topLevelDir);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("mvn dependency:build-classpath failed with exit code {}, output:\n{}", exitCode, output);
            throw new Allison1875Exception(
                    "mvn dependency:build-classpath failed with exit code " + exitCode + ", output:\n" + output);
        }

        String classpath = Files.readString(cpOutputFile.toPath(), StandardCharsets.UTF_8).trim();
        log.debug("resolved classpath: {}", classpath);

        if (classpath.isEmpty()) {
            log.warn("dependency:build-classpath returned empty classpath for: {}", mavenModuleDir);
        }

        return classpath;
    } catch (Allison1875Exception e) {
        throw e;
    } catch (Exception e) {
        throw new Allison1875Exception("failed to execute mvn dependency:build-classpath", e);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/util/MavenUtils.java
git commit -m "refactor: executeBuildClasspath() uses embedded Maven with process fallback"
```

---

### Task 5: Create single-module test resource project

**Files:**
- Create: `common/src/test/resources/maven-test-project/pom.xml`
- Create: `common/src/test/resources/maven-test-project/src/main/java/com/example/Hello.java`

- [ ] **Step 1: Create test project pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.test</groupId>
    <artifactId>maven-test-project</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.17.0</version>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Create Hello.java source file**

```java
package com.example;

import org.apache.commons.lang3.StringUtils;

public class Hello {

    public String greet(String name) {
        if (StringUtils.isBlank(name)) {
            return "Hello, World!";
        }
        return "Hello, " + name + "!";
    }
}
```

- [ ] **Step 3: Verify test project compiles**

Run: `mvn compile -f common/src/test/resources/maven-test-project/pom.xml -q`
Expected: exits 0, `target/classes/com/example/Hello.class` exists

- [ ] **Step 4: Clean target before committing**

```bash
rm -rf common/src/test/resources/maven-test-project/target
```

- [ ] **Step 5: Commit**

```bash
git add common/src/test/resources/maven-test-project/
git commit -m "test: add single-module Maven test resource project"
```

---

### Task 6: Create multi-module test resource project

**Files:**
- Create: `common/src/test/resources/multi-module-project/pom.xml`
- Create: `common/src/test/resources/multi-module-project/module-a/pom.xml`
- Create: `common/src/test/resources/multi-module-project/module-a/src/main/java/com/example/ModuleA.java`
- Create: `common/src/test/resources/multi-module-project/module-b/pom.xml`
- Create: `common/src/test/resources/multi-module-project/module-b/src/main/java/com/example/UsesA.java`

- [ ] **Step 1: Create parent pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.test</groupId>
    <artifactId>multi-module-project</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>module-a</module>
        <module>module-b</module>
    </modules>
</project>
```

- [ ] **Step 2: Create module-a pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example.test</groupId>
        <artifactId>multi-module-project</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>module-a</artifactId>
</project>
```

- [ ] **Step 3: Create ModuleA.java**

```java
package com.example;

public class ModuleA {

    public String hello() {
        return "Hello from Module A";
    }
}
```

- [ ] **Step 4: Create module-b pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example.test</groupId>
        <artifactId>multi-module-project</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>module-b</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.example.test</groupId>
            <artifactId>module-a</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: Create UsesA.java**

```java
package com.example;

public class UsesA {

    public String delegateToA() {
        ModuleA a = new ModuleA();
        return a.hello() + " (via B)";
    }
}
```

- [ ] **Step 6: Verify multi-module project compiles**

Run: `mvn compile -f common/src/test/resources/multi-module-project/pom.xml -q`
Expected: exits 0, both module-a and module-b have `target/classes/`

- [ ] **Step 7: Clean targets before committing**

```bash
rm -rf common/src/test/resources/multi-module-project/module-a/target
rm -rf common/src/test/resources/multi-module-project/module-b/target
```

- [ ] **Step 8: Commit**

```bash
git add common/src/test/resources/multi-module-project/
git commit -m "test: add multi-module Maven test resource project"
```

---

### Task 7: Write MavenUtilsTest

**Files:**
- Create: `common/src/test/java/com/spldeolin/allison1875/common/util/MavenUtilsTest.java`

- [ ] **Step 1: Write the test class with setup and compile tests**

```java
package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class MavenUtilsTest {

    private static Path tempDir;
    private static File singleModuleDir;
    private static File multiModuleDir;
    private static File moduleBDir;

    @BeforeAll
    static void setUp() throws IOException {
        tempDir = Files.createTempDirectory("maven-utils-test-");

        // Copy single-module project
        Path singleModuleSrc = Path.of("src/test/resources/maven-test-project");
        singleModuleDir = tempDir.resolve("maven-test-project").toFile();
        copyDirectory(singleModuleSrc, singleModuleDir.toPath());

        // Copy multi-module project
        Path multiModuleSrc = Path.of("src/test/resources/multi-module-project");
        multiModuleDir = tempDir.resolve("multi-module-project").toFile();
        copyDirectory(multiModuleSrc, multiModuleDir.toPath());
        moduleBDir = new File(multiModuleDir, "module-b");
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void compile_singleModule_success() {
        MavenUtils.compile(singleModuleDir, null);

        File classFile = new File(singleModuleDir, "target/classes/com/example/Hello.class");
        assertTrue(classFile.exists(), "Hello.class should exist after compile");
    }

    @Test
    void compile_multiModule_success() {
        MavenUtils.compile(moduleBDir, null);

        File moduleAClass = new File(multiModuleDir, "module-a/target/classes/com/example/ModuleA.class");
        File moduleBClass = new File(multiModuleDir, "module-b/target/classes/com/example/UsesA.class");
        assertTrue(moduleAClass.exists(), "ModuleA.class should exist (compiled via -am)");
        assertTrue(moduleBClass.exists(), "UsesA.class should exist");
    }

    @Test
    void compile_invalidDir_throwsException() {
        File nonExistent = new File("/tmp/non-existent-dir-xyz");
        assertThrows(Allison1875Exception.class, () -> MavenUtils.compile(nonExistent, null));
    }

    @Test
    void compile_nullDir_throwsException() {
        assertThrows(Allison1875Exception.class, () -> MavenUtils.compile(null, null));
    }

    @Test
    void buildClassLoader_singleModule_returnsWorkingClassLoader() throws Exception {
        ClassLoader cl = MavenUtils.buildClassLoader(singleModuleDir, null);

        assertNotNull(cl);
        // Should be able to load commons-lang3 class
        Class<?> stringUtilsClass = cl.loadClass("org.apache.commons.lang3.StringUtils");
        assertNotNull(stringUtilsClass);
    }

    @Test
    void buildClassLoader_includesTargetClasses() throws Exception {
        ClassLoader cl = MavenUtils.buildClassLoader(singleModuleDir, null);

        // Should be able to load the project's own compiled class
        Class<?> helloClass = cl.loadClass("com.example.Hello");
        assertNotNull(helloClass);
    }

    @Test
    void buildClassLoader_invalidDir_throwsException() {
        File nonExistent = new File("/tmp/non-existent-dir-xyz");
        assertThrows(Allison1875Exception.class, () -> MavenUtils.buildClassLoader(nonExistent, null));
    }

    @Test
    void compile_noPomDir_throwsException() {
        // A directory that exists but has no pom.xml — Maven will fail
        File noPomDir = tempDir.toFile();
        // compile doesn't explicitly check pom.xml, but Maven invocation will fail
        assertThrows(Allison1875Exception.class, () -> MavenUtils.compile(noPomDir, null));
    }

    @Test
    void findTopLevelProjectDir_multiModule_findsRoot() throws Exception {
        // Use reflection to test private method
        java.lang.reflect.Method method = MavenUtils.class.getDeclaredMethod(
                "findTopLevelProjectDir", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(null, moduleBDir);
        assertEquals(multiModuleDir.getCanonicalPath(), result.getCanonicalPath());
    }

    @Test
    void findTopLevelProjectDir_singleModule_returnsSelf() throws Exception {
        java.lang.reflect.Method method = MavenUtils.class.getDeclaredMethod(
                "findTopLevelProjectDir", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(null, singleModuleDir);
        assertEquals(singleModuleDir.getCanonicalPath(), result.getCanonicalPath());
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(src -> {
            Path dest = target.resolve(source.relativize(src));
            try {
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run from the `common` module directory:

```bash
mvn test -pl common -Dtest=MavenUtilsTest -Dgroups=integration -Dsurefire.useModulePath=false
```

Expected: all 10 tests pass

- [ ] **Step 3: Commit**

```bash
git add common/src/test/java/com/spldeolin/allison1875/common/util/MavenUtilsTest.java
git commit -m "test: add MavenUtilsTest integration tests"
```

---

### Task 8: Configure surefire to exclude integration tests by default

**Files:**
- Modify: `common/pom.xml`

- [ ] **Step 1: Add maven-surefire-plugin configuration**

Add inside `<build><plugins>` in `common/pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <excludedGroups>integration</excludedGroups>
    </configuration>
</plugin>
```

- [ ] **Step 2: Verify default build skips integration tests**

Run: `mvn test -pl common -q`
Expected: exits 0, MavenUtilsTest is NOT executed (0 tests run or only non-integration tests run)

- [ ] **Step 3: Verify integration tests still run with explicit tag**

Run: `mvn test -pl common -Dgroups=integration -q`
Expected: exits 0, MavenUtilsTest tests execute and pass

- [ ] **Step 4: Commit**

```bash
git add common/pom.xml
git commit -m "build: exclude integration-tagged tests from default surefire run"
```
