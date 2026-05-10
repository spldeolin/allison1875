package com.spldeolin.allison1875.cli.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * 为指定的Maven模块项目路径构建ClassLoader的工具类。
 *
 * <p>通过调用 {@code mvn dependency:build-classpath} 命令解析出目标项目编译时的所有依赖路径（包含第三方、第二方依赖），
 * 再加上该模块自身的 {@code target/classes} 目录，最终构造出一个能够加载该项目编译期所有可见类的 {@link URLClassLoader}。
 *
 * <p>注意：目标路径无需事先执行过 {@code mvn compile}，但目标机器上必须有可用的 Maven 环境（{@code mvn} 命令在 PATH 中可访问）。
 *
 * @author Deolin 2026-05-09
 */
@Slf4j
public class MavenProjectClassLoaderUtils {

    private MavenProjectClassLoaderUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 为指定的Maven模块项目根目录构建一个ClassLoader
     *
     * @param mavenModuleDir Maven模块的根目录（包含pom.xml的目录）
     * @return 能够加载该Maven模块编译期所有可见类的ClassLoader
     */
    public static ClassLoader buildClassLoader(File mavenModuleDir) {
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

        String classpath = executeBuildClasspath(mavenModuleDir);
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

    private static String executeBuildClasspath(File mavenModuleDir) {
        // 使用 -DincludeScope=compile 确保获取编译期所有依赖
        // 使用 -DmdOutputFile 将 classpath 输出到临时文件，避免解析标准输出中的噪音
        File cpOutputFile;
        try {
            cpOutputFile = File.createTempFile("allison1875-cp-", ".txt");
            cpOutputFile.deleteOnExit();
        } catch (Exception e) {
            throw new Allison1875Exception("failed to create temp file for classpath output", e);
        }

        List<String> command = new ArrayList<>();
        command.add(detectMvnCommand());
        command.add("compile");
        command.add("dependency:build-classpath");
        command.add("-DincludeScope=compile");
        command.add("-Dmdep.outputFile=" + cpOutputFile.getAbsolutePath());
        command.add("-q"); // quiet mode，减少输出噪音

        log.info("executing: {} (in {})", String.join(" ", command), mavenModuleDir);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(mavenModuleDir);
            pb.redirectErrorStream(true);
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
            String classpath = org.apache.commons.io.FileUtils.readFileToString(cpOutputFile, StandardCharsets.UTF_8)
                    .trim();
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
