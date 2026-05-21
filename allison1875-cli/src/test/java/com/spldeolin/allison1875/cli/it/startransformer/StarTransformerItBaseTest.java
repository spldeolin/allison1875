package com.spldeolin.allison1875.cli.it.startransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.cli.Bootstrap;

/**
 * star-transformer集成测试基类。
 *
 * <p>封装了通用流程：
 * ①将 test/resources/it/star-transformer/{caseName}/ 下的测试资源递归拷贝到 target/it/st-{caseName}/ 临时工作目录；
 * ②读取 .allison1875.yml 并将所有相对路径（*Module、mapperXmlDirs）解析为绝对路径后回写yml；
 * ③调用 {@link Bootstrap#main(String[])} 先执行persistence-generator生成Entity/Design/Mapper/XML；
 * ④将 src/main/java-st/ 下的源码拷贝到 src/main/java/（模拟原invoker中maven-resources-plugin的copy-st-sources）；
 * ⑤调用 {@link Bootstrap#main(String[])} 再执行star-transformer解析StarSchema DSL链并转换为多表查询+装配代码；
 * ⑥子类通过 {@link #basedir} 引用临时工作目录，在 @Test 方法中编写断言。
 *
 * <p>star-transformer 依赖 persistence-generator 产生的 Design 类（含有 PropertyName 字段作为 DSL 入参），
 * 因此需要先执行 persistence-generator，再拷贝 java-st 源码，最后执行 star-transformer。
 *
 * @author Deolin 2026-05-20
 */
public abstract class StarTransformerItBaseTest {

    /**
     * 临时工作目录（target/it/st-{caseName}/），子类用此引用做断言
     */
    protected File basedir;

    /**
     * 执行star-transformer（单domain，先跑persistence-generator再跑star-transformer）
     */
    protected void runStarTransformer(String caseName) {
        runStarTransformer(caseName, null);
    }

    /**
     * 执行star-transformer（指定domainName）
     */
    protected void runStarTransformer(String caseName, String domainName) {
        // 1. 复制测试资源到临时工作目录（使用st-前缀）
        basedir = copyResourceToWorkDir(caseName);

        // 2. 解析yml中的相对路径为绝对路径并回写
        File configFile = resolveAndRewriteConfig(basedir);

        // 保存当前线程的context classloader
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 3. 先执行 persistence-generator，生成 Entity/Design/Mapper/XML
            List<String> pgArgs = new ArrayList<>();
            pgArgs.add("--tool=persistence-generator");
            pgArgs.add("--config=" + configFile.getAbsolutePath());
            if (domainName != null && !domainName.isEmpty()) {
                pgArgs.add("--domain=" + domainName);
            }
            Bootstrap.main(pgArgs.toArray(new String[0]));

            // 4. 将 java-st/ 下的源码拷贝到 src/main/java/（模拟maven-resources-plugin的copy-st-sources）
            copyJavaStToJava(basedir);

            // 恢复classloader（persistence-generator可能修改了它），让star-transformer的MavenProjectClassLoaderUtils
            // 能从干净的状态重新构建classloader
            Thread.currentThread().setContextClassLoader(originalClassLoader);

            // 5. 执行 star-transformer
            List<String> stArgs = new ArrayList<>();
            stArgs.add("--tool=star-transformer");
            stArgs.add("--config=" + configFile.getAbsolutePath());
            if (domainName != null && !domainName.isEmpty()) {
                stArgs.add("--domain=" + domainName);
            }
            Bootstrap.main(stArgs.toArray(new String[0]));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * 将 classpath 下 it/star-transformer/{caseName}/ 整个目录递归拷贝到 target/it/st-{caseName}/
     */
    private File copyResourceToWorkDir(String caseName) {
        String resourcePrefix = "it/star-transformer/" + caseName;
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePrefix);
        if (resourceUrl == null) {
            throw new RuntimeException("测试资源目录不存在: " + resourcePrefix);
        }

        Path sourcePath = Paths.get(resourceUrl.getPath());
        Path targetPath = Paths.get("target", "it", "st-" + caseName);

        try {
            if (Files.exists(targetPath)) {
                deleteRecursively(targetPath);
            }
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path relative = sourcePath.relativize(dir);
                    Path target = targetPath.resolve(relative);
                    Files.createDirectories(target);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = sourcePath.relativize(file);
                    Path target = targetPath.resolve(relative);
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("拷贝测试资源失败: " + caseName, e);
        }

        return targetPath.toFile();
    }

    /**
     * 将 src/main/java-st/ 下的源码递归拷贝到 src/main/java/
     *
     * <p>模拟原invoker模式中pom.xml里maven-resources-plugin的copy-st-sources execution，
     * 在persistence-generator执行后（Design已生成）、star-transformer执行前，
     * 将含有StarSchema DSL链的Service源码放入src/main/java供star-transformer解析。
     */
    private void copyJavaStToJava(File basedir) {
        File javaStDir = new File(basedir, "src/main/java-st");
        if (!javaStDir.exists()) {
            throw new RuntimeException("java-st目录不存在: " + javaStDir.getAbsolutePath());
        }
        File javaDir = new File(basedir, "src/main/java");

        Path source = javaStDir.toPath();
        Path target = javaDir.toPath();

        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path relative = source.relativize(dir);
                    Path destDir = target.resolve(relative);
                    Files.createDirectories(destDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = source.relativize(file);
                    Path destFile = target.resolve(relative);
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("拷贝java-st到java失败", e);
        }
    }

    /**
     * 读取 .allison1875.yml（以Map模式），将所有相对路径解析为绝对路径后回写到磁盘。
     */
    @SuppressWarnings("unchecked")
    private File resolveAndRewriteConfig(File basedir) {
        File configFile = new File(basedir, ".allison1875.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> configMap;
        try (InputStream fis = new FileInputStream(configFile)) {
            configMap = yaml.load(fis);
        } catch (IOException e) {
            throw new UncheckedIOException("读取配置文件失败: " + configFile.getAbsolutePath(), e);
        }

        String basedirAbsPath = getCanonicalPath(basedir);

        List<Map<String, Object>> domains = (List<Map<String, Object>>) configMap.get("domains");
        if (domains != null) {
            String[] moduleKeys = {"controllerModule", "dtoModule", "enumModule", "serviceModule", "serviceImplModule",
                    "persistenceModule"};
            for (Map<String, Object> domain : domains) {
                for (String key : moduleKeys) {
                    String value = (String) domain.get(key);
                    domain.put(key, resolveModuleAbsolutePath(basedirAbsPath, value));
                }

                // 解析 mapperXmlDirs 中的相对路径为基于basedir的绝对路径
                Object mapperXmlDirsObj = domain.get("mapperXmlDirs");
                if (mapperXmlDirsObj instanceof List) {
                    List<String> resolved = ((List<Object>) mapperXmlDirsObj).stream()
                            .map(d -> resolveFileRelativeToBasedir(basedir, new File(d.toString())).getPath())
                            .collect(Collectors.toList());
                    domain.put("mapperXmlDirs", resolved);
                }
            }
        }

        // 回写yml到磁盘
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        Yaml dumpYaml = new Yaml(dumperOptions);
        try (FileWriter writer = new FileWriter(configFile)) {
            dumpYaml.dump(configMap, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("回写配置文件失败: " + configFile.getAbsolutePath(), e);
        }

        return configFile;
    }

    private String resolveModuleAbsolutePath(String basedirAbsPath, String modulePath) {
        if (modulePath == null || modulePath.isEmpty() || ".".equals(modulePath)) {
            return basedirAbsPath;
        }
        return getCanonicalPath(new File(basedirAbsPath, modulePath));
    }

    private File resolveFileRelativeToBasedir(File basedir, File file) {
        try {
            return basedir.toPath().resolve(file.toPath()).toFile().getCanonicalFile();
        } catch (IOException e) {
            return file;
        }
    }

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
