package com.spldeolin.allison1875.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * 为指定的Maven模块项目路径构建ClassLoader的工具类。
 *
 * <p>通过调用 {@code mvn dependency:build-classpath} 命令解析出目标项目编译时的所有依赖路径（包含第三方、第二方依赖），
 * 再加上该模块自身的 {@code target/classes} 目录，最终构造出一个能够加载该项目编译期所有可见类的 {@link URLClassLoader}。
 *
 * <p>注意：目标路径无需事先执行过 {@code mvn compile}，但目标机器上必须有可用的 Maven 环境（{@code mvn} 命令在 PATH 中可访问）。
 * 如果目标项目要求特定的Java版本编译，用户可在{@code .allison1875.yml}中通过{@code javaHome}配置项
 * 指定JDK安装目录路径，执行mvn子进程时会自动设置{@code JAVA_HOME}环境变量。
 *
 * @author Deolin 2026-05-09
 */
@Slf4j
public class MavenProjectClassLoaderUtils {

    private MavenProjectClassLoaderUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

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

        StringBuilder commandLine = buildMvnCommandPrefix(javaHome);
        commandLine.append(" compile");
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

    private static String executeBuildClasspath(File mavenModuleDir, String javaHome) {
        // 使用 -DincludeScope=compile 确保获取编译期所有依赖
        // 使用 -DmdOutputFile 将 classpath 输出到临时文件，避免解析标准输出中的噪音
        File cpOutputFile;
        try {
            cpOutputFile = File.createTempFile("allison1875-cp-", ".txt");
            cpOutputFile.deleteOnExit();
        } catch (Exception e) {
            throw new Allison1875Exception("failed to create temp file for classpath output", e);
        }

        File topLevelDir = findTopLevelProjectDir(mavenModuleDir);

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
        commandLine.append(" -q"); // quiet mode，减少输出噪音

        log.info("executing: {} (in {})", commandLine, topLevelDir);

        try {
            ProcessBuilder pb = createShellProcessBuilder(commandLine.toString(), topLevelDir);
            Process process = pb.start();

            // 读取并记录子进程输出（调试用）
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

            // 从临时文件中读取 classpath 字符串
            String classpath = FileUtils.readFileToString(cpOutputFile, StandardCharsets.UTF_8).trim();
            log.debug("resolved classpath:\n{}", classpath);

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
        if (javaHome != null && !javaHome.isEmpty()) {
            sb.append("JAVA_HOME=").append(javaHome).append(' ');
            log.info("using specified JAVA_HOME: {}", javaHome);
        }
        sb.append(detectMvnCommand());
        return sb;
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
                String pomContent = FileUtils.readFileToString(parentPom, StandardCharsets.UTF_8);
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
