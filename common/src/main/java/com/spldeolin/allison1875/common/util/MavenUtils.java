package com.spldeolin.allison1875.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * Maven工具类，主要用途如下：
 * 1. 对一个maven项目执行mvn compile
 * 2. 基于mvn dependency:build-classpath，为一个maven项目构建classloader
 * 3. 基于mvn source:jar install dependency:sources，为一个maven项目遍历他所依赖的源码并将源码转化为CU
 * <p>
 * 使用前需要确保环境安装了maven
 *
 * @author Deolin 2026-05-09
 */
@Slf4j
public class MavenUtils {

    private MavenUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    private static final ReentrantLock MAVEN_INVOKE_LOCK = new ReentrantLock();
    private static volatile ClassLoader mavenClassLoader;
    private static volatile Method mavenDoMainMethod;
    private static volatile Object mavenCliInstance;
    private static volatile boolean embeddedMavenAvailable = true;

    /**
     * 对指定的Maven模块执行 mvn compile
     *
     * <p>如果该模块属于多模块Maven项目（存在上层聚合pom），则会自动在顶层项目目录执行
     * {@code mvn compile -pl <模块相对路径> -am}，确保该模块依赖的兄弟模块也会被编译。
     *
     * @param mavenModuleDir Maven模块的根目录（包含pom.xml的目录）
     * @param javaHome JDK安装目录路径，为null时使用系统默认的JDK
     */
    public static void compile(File mavenModuleDir, String javaHome) {
        if (mavenModuleDir == null) {
            throw new Allison1875Exception("mavenModuleDir must not be null");
        }
        if (!mavenModuleDir.isDirectory()) {
            throw new Allison1875Exception("mavenModuleDir is not a directory: " + mavenModuleDir);
        }

        File topLevelDir = findTopLevelProjectDir(mavenModuleDir);

        // Build args for embedded Maven
        List<String> argsList = new ArrayList<>();
        argsList.add("compile");
        argsList.add("-T");
        argsList.add("1C");
        if (!topLevelDir.equals(mavenModuleDir)) {
            String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
            argsList.add("-pl");
            argsList.add(relativePath);
            argsList.add("-am");
        }
        if (javaHome != null && !javaHome.isEmpty()) {
            argsList.add("-Dmaven.compiler.fork=true");
            argsList.add("-Dmaven.compiler.executable=" + javaHome + "/bin/javac");
        }
        argsList.add("-q");

        String[] args = argsList.toArray(new String[0]);
        log.info("invoking embedded Maven compile with args: {} (in {})", argsList, topLevelDir);

        int exitCode = invokeEmbeddedMaven(args, topLevelDir);

        if (exitCode == -1) {
            // Embedded Maven unavailable, fall back to process-based invocation
            log.info("falling back to process-based Maven invocation for compile");
            compileViaProcess(mavenModuleDir, javaHome, topLevelDir);
            return;
        }

        if (exitCode != 0) {
            throw new Allison1875Exception("mvn compile failed with exit code " + exitCode);
        }

        log.info("mvn compile succeeded for: {}", mavenModuleDir);
    }

    private static void compileViaProcess(File mavenModuleDir, String javaHome, File topLevelDir) {
        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" compile");
        commandLine.append(" -T 1C");
        if (!topLevelDir.equals(mavenModuleDir)) {
            String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
            commandLine.append(" -pl ").append(relativePath);
            commandLine.append(" -am");
        }
        commandLine.append(" -q");

        log.info("executing: {} (in {})", commandLine, topLevelDir);

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

    /**
     * 为指定的Maven模块项目根目录构建一个ClassLoader
     *
     * @param mavenModuleDir Maven模块的根目录（包含pom.xml的目录）
     * @param javaHome JDK安装目录路径，为null时使用系统默认的JDK
     * @return 能够加载该Maven模块编译期所有可见类的ClassLoader
     */
    public static ClassLoader buildClassLoader(File mavenModuleDir, String javaHome) {
        if (mavenModuleDir == null) {
            throw new Allison1875Exception("mavenModuleDir must not be null");
        }
        if (!mavenModuleDir.isDirectory()) {
            throw new Allison1875Exception("mavenModuleDir is not a directory: " + mavenModuleDir);
        }
        File pomFile = new File(mavenModuleDir, "pom.xml");
        if (!pomFile.exists()) {
            throw new Allison1875Exception("pom.xml not found in: " + mavenModuleDir);
        }

        String classpath = executeBuildClasspath(mavenModuleDir, javaHome);
        List<URL> urls = parseClasspathToUrls(classpath);

        // 加上 target/classes 目录
        File targetClasses = new File(mavenModuleDir, "target/classes");
        try {
            urls.add(targetClasses.toURI().toURL());
            log.debug("added target/classes: {}", targetClasses);
        } catch (Exception e) {
            throw new Allison1875Exception("failed to convert target/classes to URL", e);
        }

        URL[] urlArray = urls.toArray(new URL[0]);
        log.info("built ClassLoader with {} URLs for maven module: {}", urlArray.length, mavenModuleDir);
        return new URLClassLoader(urlArray, null);
    }

    /**
     * 解析指定Maven模块所有compile范围依赖的源码，通过Consumer逐个消费每个CompilationUnit以避免OOM
     *
     * <p>处理步骤：
     * <ol>
     *     <li>从输入目录向上递归查找根module（最上层包含pom.xml的目录）</li>
     *     <li>在根module目录执行 {@code mvn source:jar install -DskipTests -q}</li>
     *     <li>通过 {@code mvn help:evaluate} 获取本地仓库路径，通过 {@code mvn dependency:list} 获取所有依赖的GAV</li>
     *     <li>对每个依赖的 -sources.jar 中的 .java 文件调用 {@link CompilationUnitUtils#parseJava(InputStream, String)}
     *         解析为CU，解析完成后立即通过cuConsumer消费，不在内存中积累</li>
     * </ol>
     *
     * @param mavenModuleDir Maven模块的根目录（包含pom.xml的目录）
     * @param javaHome JDK安装目录路径，为null时使用系统默认的JDK
     * @param cuConsumer 消费每个解析出的CompilationUnit的回调
     */
    public static void consumeDependencySourceCus(File mavenModuleDir, String javaHome,
            Consumer<CompilationUnit> cuConsumer) {
        if (mavenModuleDir == null) {
            throw new Allison1875Exception("mavenModuleDir must not be null");
        }
        if (!mavenModuleDir.isDirectory()) {
            throw new Allison1875Exception("mavenModuleDir is not a directory: " + mavenModuleDir);
        }
        if (cuConsumer == null) {
            throw new Allison1875Exception("cuConsumer must not be null");
        }

        // 1. 向上递归查找根module
        File rootModuleDir = findTopLevelProjectDir(mavenModuleDir);
        log.info("root module dir: {}", rootModuleDir);

        // 2. 在根module目录执行 mvn source:jar install -DskipTests -q
        executeSourceJarInstall(rootModuleDir, javaHome);

        // 3. 获取本地仓库路径和所有compile范围依赖的GAV
        String localRepoPath = resolveLocalRepoPath(mavenModuleDir, javaHome);
        List<String> gavs = resolveDependencyGavs(mavenModuleDir, javaHome);
        log.info("resolved {} compile-scope dependencies, localRepo: {}", gavs.size(), localRepoPath);

        // 4. 逐个解析sources.jar中的.java文件并消费CU
        int parsedCount = 0;
        int skippedCount = 0;
        for (String gav : gavs) {
            File sourcesJar = gavToSourcesJarFile(localRepoPath, gav);
            if (!sourcesJar.exists()) {
                log.debug("sources.jar not found for {}, skipping", gav);
                skippedCount++;
                continue;
            }
            parsedCount += parseAndConsumeSourcesJar(sourcesJar, gav, cuConsumer);
        }
        log.info(
                "consumeDependencySourceCus completed. parsed {} CUs from {} dependencies ({} skipped, no sources.jar)",
                parsedCount, gavs.size(), skippedCount);
    }

    private static void executeSourceJarInstall(File rootModuleDir, String javaHome) {
        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" source:jar install dependency:sources -DskipTests");

        log.info("executing: {} (in {})", commandLine, rootModuleDir);

        try {
            ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), rootModuleDir);
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
                log.error("mvn source:jar install failed with exit code {}, output:\n{}", exitCode, output);
                throw new Allison1875Exception(
                        "mvn source:jar install failed with exit code " + exitCode + ", output:\n" + output);
            }
            log.info("mvn source:jar install succeeded for: {}", rootModuleDir);
        } catch (Allison1875Exception e) {
            throw e;
        } catch (Exception e) {
            throw new Allison1875Exception("failed to execute mvn source:jar install", e);
        }
    }

    private static String resolveLocalRepoPath(File mavenModuleDir, String javaHome) {
        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" help:evaluate -Dexpression=settings.localRepository -DforceStdout -q");

        log.info("executing: {} (in {})", commandLine, mavenModuleDir);

        try {
            ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), mavenModuleDir);
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
                log.error("mvn help:evaluate failed with exit code {}, output:\n{}", exitCode, output);
                throw new Allison1875Exception(
                        "mvn help:evaluate failed with exit code " + exitCode + ", output:\n" + output);
            }

            String localRepo = output.toString().trim();
            if (localRepo.isEmpty()) {
                throw new Allison1875Exception("mvn help:evaluate returned empty localRepository");
            }
            return localRepo;
        } catch (Allison1875Exception e) {
            throw e;
        } catch (Exception e) {
            throw new Allison1875Exception("failed to execute mvn help:evaluate", e);
        }
    }

    private static List<String> resolveDependencyGavs(File mavenModuleDir, String javaHome) {
        File gavOutputFile;
        try {
            gavOutputFile = File.createTempFile("allison1875-deps-", ".txt");
            gavOutputFile.deleteOnExit();
        } catch (Exception e) {
            throw new Allison1875Exception("failed to create temp file for dependency:list output", e);
        }

        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" dependency:list -DincludeScope=compile -DoutputAbsoluteArtifactFilename=false");
        commandLine.append(" -DoutputFile=").append(gavOutputFile.getAbsolutePath());

        log.info("executing: {} (in {})", commandLine, mavenModuleDir);

        try {
            ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), mavenModuleDir);
            Process process = pb.start();

            // 消费stdout避免进程阻塞
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
                log.error("mvn dependency:list failed with exit code {}, output:\n{}", exitCode, output);
                throw new Allison1875Exception(
                        "mvn dependency:list failed with exit code " + exitCode + ", output:\n" + output);
            }

            // 从输出文件中逐行解析GAV
            // -DoutputFile 格式形如: "   groupId:artifactId:type:version:scope"
            List<String> gavs = new ArrayList<>();
            List<String> lines = Files.readAllLines(gavOutputFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(":");
                if (parts.length >= 4) {
                    // groupId:artifactId:type:version[:scope]
                    String groupId = parts[0];
                    String artifactId = parts[1];
                    // parts[2] is type (jar/pom/etc.)
                    String version = parts[3];
                    gavs.add(groupId + ":" + artifactId + ":" + version);
                }
            }

            return gavs;
        } catch (Allison1875Exception e) {
            throw e;
        } catch (Exception e) {
            throw new Allison1875Exception("failed to execute mvn dependency:list", e);
        } finally {
            gavOutputFile.delete();
        }
    }

    /**
     * 将GAV字符串转换为对应的-sources.jar文件路径
     *
     * @param localRepoPath 本地Maven仓库路径
     * @param gav GAV字符串，格式为 "groupId:artifactId:version"
     * @return 对应的-sources.jar文件
     */
    private static File gavToSourcesJarFile(String localRepoPath, String gav) {
        String[] parts = gav.split(":");
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        // e.g.: /repo/com/google/guava/guava/33.4.0-jre/guava-33.4.0-jre-sources.jar
        String path =
                localRepoPath + File.separator + groupId.replace('.', File.separatorChar) + File.separator + artifactId
                        + File.separator + version + File.separator + artifactId + "-" + version + "-sources.jar";
        return new File(path);
    }

    private static int parseAndConsumeSourcesJar(File sourcesJar, String gav, Consumer<CompilationUnit> cuConsumer) {
        int count = 0;
        try (JarFile jarFile = new JarFile(sourcesJar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".java")) {
                    continue;
                }
                try (InputStream is = jarFile.getInputStream(entry)) {
                    String sourceName = gav + "!/" + entry.getName();
                    CompilationUnit cu = CompilationUnitUtils.parseJava(is, sourceName);
                    cuConsumer.accept(cu);
                    count++;
                } catch (Exception e) {
                    log.warn("failed to parse {} in {}, skipping", entry.getName(), gav, e);
                }
            }
        } catch (Exception e) {
            log.warn("failed to read sources.jar for {}: {}", gav, e.getMessage());
        }
        return count;
    }

    private static String executeBuildClasspath(File mavenModuleDir, String javaHome) {
        // 使用 -DincludeScope=compile 确保获取编译期所有依赖
        // 使用 -Dmdep.outputFile 将 classpath 输出到临时文件，避免解析标准输出中的噪音
        File cpOutputFile;
        try {
            cpOutputFile = File.createTempFile("allison1875-cp-", ".txt");
            cpOutputFile.deleteOnExit();
        } catch (Exception e) {
            throw new Allison1875Exception("failed to create temp file for classpath output", e);
        }

        File topLevelDir = findTopLevelProjectDir(mavenModuleDir);

        try {
            // Build args for embedded Maven
            List<String> argsList = new ArrayList<>();
            argsList.add("compile");
            argsList.add("dependency:build-classpath");
            argsList.add("-T");
            argsList.add("1C");
            argsList.add("-DincludeScope=compile");
            argsList.add("-Dmdep.outputFile=" + cpOutputFile.getAbsolutePath());
            if (!topLevelDir.equals(mavenModuleDir)) {
                String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
                argsList.add("-pl");
                argsList.add(relativePath);
                argsList.add("-am");
            }
            if (javaHome != null && !javaHome.isEmpty()) {
                argsList.add("-Dmaven.compiler.fork=true");
                argsList.add("-Dmaven.compiler.executable=" + javaHome + "/bin/javac");
            }
            argsList.add("-q");

            String[] args = argsList.toArray(new String[0]);
            log.info("invoking embedded Maven build-classpath with args: {} (in {})", argsList, topLevelDir);

            int exitCode = invokeEmbeddedMaven(args, topLevelDir);

            if (exitCode == -1) {
                // Embedded Maven unavailable, fall back to process-based invocation
                log.info("falling back to process-based Maven invocation for build-classpath");
                return executeBuildClasspathViaProcess(mavenModuleDir, javaHome, topLevelDir, cpOutputFile);
            }

            if (exitCode != 0) {
                throw new Allison1875Exception(
                        "mvn dependency:build-classpath failed with exit code " + exitCode);
            }

            // Read classpath from temp file
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

    private static String executeBuildClasspathViaProcess(File mavenModuleDir, String javaHome,
            File topLevelDir, File cpOutputFile) {
        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" compile");
        commandLine.append(" dependency:build-classpath");
        commandLine.append(" -T 1C");
        commandLine.append(" -DincludeScope=compile");
        commandLine.append(" -Dmdep.outputFile=").append(cpOutputFile.getAbsolutePath());
        if (!topLevelDir.equals(mavenModuleDir)) {
            String relativePath = calculateRelativePath(topLevelDir, mavenModuleDir);
            commandLine.append(" -pl ").append(relativePath);
            commandLine.append(" -am");
        }
        commandLine.append(" -q");

        log.info("executing: {} (in {})", commandLine, topLevelDir);

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

    /**
     * 构建mvn命令前缀，当指定了javaHome时在命令前追加{@code JAVA_HOME=/path/to/jdk}
     *
     * <p>最终拼出的命令形如：
     * <ul>
     *     <li>javaHome为null时：{@code mvn}</li>
     *     <li>javaHome非null时：{@code JAVA_HOME=/path/to/jdk mvn}</li>
     * </ul>
     *
     * @param javaHome JDK安装目录路径，可为null
     * @return 包含mvn命令前缀的StringBuilder，调用方可继续append子命令和参数
     */
    private static StringBuilder buildMvnCommandPrefix(String javaHome) {
        StringBuilder sb = new StringBuilder();
        sb.append(detectMvnCommand());
        if (javaHome != null && !javaHome.isEmpty()) {
            sb.append(" -Dmaven.compiler.fork=true").append(" -Dmaven.compiler.executable=\"").append(javaHome)
                    .append("/bin/javac").append("\"");
            log.info("using specified JAVA_HOME: {}", javaHome);
        }
        return sb;
    }

    // ========== Embedded Maven Invoker ==========

    /**
     * 解析Maven安装目录
     *
     * @return Maven安装目录，如果无法找到则返回null
     */
    private static File resolveMavenHome() {
        // (a) Check MAVEN_HOME env var
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null && !mavenHome.isEmpty()) {
            File home = new File(mavenHome);
            if (new File(home, "lib").isDirectory()) {
                log.debug("resolveMavenHome: found via MAVEN_HOME={}", mavenHome);
                return home;
            }
        }

        // (b) Check M2_HOME env var
        String m2Home = System.getenv("M2_HOME");
        if (m2Home != null && !m2Home.isEmpty()) {
            File home = new File(m2Home);
            if (new File(home, "lib").isDirectory()) {
                log.debug("resolveMavenHome: found via M2_HOME={}", m2Home);
                return home;
            }
        }

        // (c) Run 'which mvn', follow symlinks, navigate up from bin/mvn
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "mvn");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String mvnPath;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                mvnPath = reader.readLine();
            }
            int exitCode = process.waitFor();
            if (exitCode == 0 && mvnPath != null && !mvnPath.trim().isEmpty()) {
                // Follow symlinks to real path
                Path realPath = Paths.get(mvnPath.trim()).toRealPath();
                // Navigate up from bin/mvn to parent (Maven home)
                File home = realPath.getParent().getParent().toFile();
                if (new File(home, "lib").isDirectory()) {
                    log.debug("resolveMavenHome: found via 'which mvn' -> {}", home);
                    return home;
                }
            }
        } catch (Exception e) {
            log.debug("resolveMavenHome: 'which mvn' symlink approach failed: {}", e.getMessage());
        }

        // (d) Parse 'mvn --version' output for "Maven home:" line (handles shims like jenv)
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Maven home:")) {
                        String homePath = line.substring("Maven home:".length()).trim();
                        File home = new File(homePath);
                        if (new File(home, "lib").isDirectory()) {
                            log.debug("resolveMavenHome: found via 'mvn --version' -> {}", home);
                            process.waitFor();
                            return home;
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.debug("resolveMavenHome: 'mvn --version' failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 使用双重检查锁定延迟初始化嵌入式Maven
     */
    private static void initEmbeddedMaven() {
        if (mavenClassLoader != null) {
            return;
        }
        if (!embeddedMavenAvailable) {
            return;
        }
        synchronized (MavenUtils.class) {
            if (mavenClassLoader != null) {
                return;
            }
            if (!embeddedMavenAvailable) {
                return;
            }

            File mavenHome = resolveMavenHome();
            if (mavenHome == null) {
                embeddedMavenAvailable = false;
                log.warn("embedded Maven unavailable: cannot resolve Maven home directory, "
                        + "falling back to process-based invocation");
                return;
            }

            try {
                List<URL> jarUrls = new ArrayList<>();

                // Scan MAVEN_HOME/boot/*.jar
                File bootDir = new File(mavenHome, "boot");
                if (bootDir.isDirectory()) {
                    File[] bootJars = bootDir.listFiles((dir, name) -> name.endsWith(".jar"));
                    if (bootJars != null) {
                        for (File jar : bootJars) {
                            jarUrls.add(jar.toURI().toURL());
                        }
                    }
                }

                // Scan MAVEN_HOME/lib/*.jar
                File libDir = new File(mavenHome, "lib");
                if (libDir.isDirectory()) {
                    File[] libJars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
                    if (libJars != null) {
                        for (File jar : libJars) {
                            jarUrls.add(jar.toURI().toURL());
                        }
                    }
                }

                // Scan MAVEN_HOME/lib/ext/*.jar
                File extDir = new File(mavenHome, "lib/ext");
                if (extDir.isDirectory()) {
                    File[] extJars = extDir.listFiles((dir, name) -> name.endsWith(".jar"));
                    if (extJars != null) {
                        for (File jar : extJars) {
                            jarUrls.add(jar.toURI().toURL());
                        }
                    }
                }

                if (jarUrls.isEmpty()) {
                    embeddedMavenAvailable = false;
                    log.warn("embedded Maven unavailable: no JARs found in {}", mavenHome);
                    return;
                }

                // Create isolated ClassLoader (platform classloader parent = access to JDK classes
                // like javax.tools.JavaCompiler, but NOT application classes — Guice isolation preserved)
                URLClassLoader cl = new URLClassLoader(jarUrls.toArray(new URL[0]),
                        ClassLoader.getPlatformClassLoader());

                // Reflectively load MavenCli
                Class<?> mavenCliClass = cl.loadClass("org.apache.maven.cli.MavenCli");
                Object cliInstance = mavenCliClass.getDeclaredConstructor().newInstance();
                Method doMainMethod = mavenCliClass.getMethod("doMain", String[].class, String.class,
                        PrintStream.class, PrintStream.class);

                mavenClassLoader = cl;
                mavenCliInstance = cliInstance;
                mavenDoMainMethod = doMainMethod;

                log.info("embedded Maven initialized successfully from: {} ({} JARs loaded)",
                        mavenHome, jarUrls.size());
            } catch (Exception e) {
                embeddedMavenAvailable = false;
                log.warn("embedded Maven unavailable: initialization failed, "
                        + "falling back to process-based invocation", e);
            }
        }
    }

    /**
     * 通过嵌入式Maven调用doMain方法
     *
     * @param args Maven命令参数
     * @param workingDir 工作目录
     * @return 退出码，-1表示嵌入式Maven不可用
     */
    private static int invokeEmbeddedMaven(String[] args, File workingDir) {
        initEmbeddedMaven();
        if (!embeddedMavenAvailable) {
            return -1;
        }

        MAVEN_INVOKE_LOCK.lock();
        String previousMultiModuleDir = System.getProperty("maven.multiModuleProjectDirectory");
        Thread currentThread = Thread.currentThread();
        ClassLoader originalCl = currentThread.getContextClassLoader();
        try {
            System.setProperty("maven.multiModuleProjectDirectory", workingDir.getAbsolutePath());
            currentThread.setContextClassLoader(mavenClassLoader);

            ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
            ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
            try (PrintStream outStream = new PrintStream(outBaos, true, StandardCharsets.UTF_8);
                 PrintStream errStream = new PrintStream(errBaos, true, StandardCharsets.UTF_8)) {

                int exitCode = (int) mavenDoMainMethod.invoke(mavenCliInstance, args,
                        workingDir.getAbsolutePath(), outStream, errStream);

                if (exitCode != 0) {
                    String output = outBaos.toString(StandardCharsets.UTF_8)
                            + errBaos.toString(StandardCharsets.UTF_8);
                    log.error("embedded Maven failed with exit code {}, output:\n{}", exitCode, output);
                }
                return exitCode;
            }
        } catch (Exception e) {
            log.error("embedded Maven invocation failed", e);
            return -1;
        } finally {
            currentThread.setContextClassLoader(originalCl);
            if (previousMultiModuleDir == null) {
                System.clearProperty("maven.multiModuleProjectDirectory");
            } else {
                System.setProperty("maven.multiModuleProjectDirectory", previousMultiModuleDir);
            }
            MAVEN_INVOKE_LOCK.unlock();
        }
    }

    /**
     * 通过{@code sh -c}创建ProcessBuilder，以支持命令行中包含{@code JAVA_HOME=}等环境变量前缀
     *
     * @param commandLine 完整的shell命令行字符串
     * @param workingDir 工作目录
     * @return 配置好的ProcessBuilder
     */
    private static ProcessBuilder createShellProcessBuilder(String commandLine, File workingDir) {
        List<String> command = new ArrayList<>();
        command.add("sh");
        command.add("-c");
        command.add(commandLine);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);
        return pb;
    }

    private static List<URL> parseClasspathToUrls(String classpath) {
        List<URL> urls = new ArrayList<>();
        if (classpath == null || classpath.isEmpty()) {
            return urls;
        }

        String separator = File.pathSeparator; // ":" on Unix, ";" on Windows
        String[] paths = classpath.split(separator);
        for (String path : paths) {
            path = path.trim();
            if (path.isEmpty()) {
                continue;
            }
            try {
                File file = new File(path);
                urls.add(file.toURI().toURL());
            } catch (Exception e) {
                log.warn("failed to convert classpath entry to URL: {}, skipping", path, e);
            }
        }

        return urls;
    }

    /**
     * 从指定的Maven模块目录向上查找顶层Maven项目目录。
     *
     * <p>逐层向上检查父目录是否包含pom.xml且是聚合模块（包含{@code <modules>}标签），
     * 直到父目录不再包含pom.xml，返回最后一个包含pom.xml的目录作为顶层项目目录。
     *
     * @param mavenModuleDir Maven模块的根目录
     * @return 顶层Maven项目目录（如果mavenModuleDir本身就是顶层，则返回自身）
     */
    private static File findTopLevelProjectDir(File mavenModuleDir) {
        File current = mavenModuleDir;
        while (true) {
            File parentDir = current.getParentFile();
            if (parentDir == null) {
                break;
            }
            File parentPom = new File(parentDir, "pom.xml");
            if (!parentPom.exists()) {
                break;
            }
            // 检查父目录的pom.xml是否是聚合模块（包含<modules>标签）
            try {
                String pomContent = Files.readString(parentPom.toPath(), StandardCharsets.UTF_8);
                if (!pomContent.contains("<modules>")) {
                    break;
                }
            } catch (Exception e) {
                log.warn("failed to read pom.xml at {}, stopping upward search", parentDir, e);
                break;
            }
            current = parentDir;
        }
        if (!current.equals(mavenModuleDir)) {
            log.info("found top-level Maven project dir: {} (from module: {})", current, mavenModuleDir);
        }
        return current;
    }

    /**
     * 计算子模块目录相对于顶层项目目录的相对路径
     *
     * @param topLevelDir 顶层项目目录
     * @param moduleDir 子模块目录
     * @return 相对路径字符串（使用"/"分隔符，适用于Maven -pl参数）
     */
    private static String calculateRelativePath(File topLevelDir, File moduleDir) {
        Path topPath = topLevelDir.toPath().toAbsolutePath().normalize();
        Path modulePath = moduleDir.toPath().toAbsolutePath().normalize();
        Path relativePath = topPath.relativize(modulePath);
        // Maven -pl 参数使用"/"作为路径分隔符
        return relativePath.toString().replace(File.separatorChar, '/');
    }

    private static String detectMvnCommand() {
        // 优先使用 MAVEN_HOME 或 M2_HOME 环境变量
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome != null) {
            File mvnBin = new File(mavenHome, "bin/mvn");
            if (mvnBin.exists()) {
                return mvnBin.getAbsolutePath();
            }
        }
        // 回退到 PATH 中的 mvn
        return "mvn";
    }

}
