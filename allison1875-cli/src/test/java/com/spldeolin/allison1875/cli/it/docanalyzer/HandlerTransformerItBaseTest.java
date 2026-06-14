package com.spldeolin.allison1875.cli.it.docanalyzer;

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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.cli.Entrypoint;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.DomainContext;

/**
 * handler-transformer集成测试基类。
 *
 * <p>封装了通用流程：
 * ①将 test/resources/it/handler-transformer/{caseName}/ 下的测试资源递归拷贝到 target/it/{caseName}/ 临时工作目录；
 * ②读取 .allison1875.yml 并将所有相对路径（*Module）解析为绝对路径后回写yml；
 * ③组装CLI参数并调用 {@link Entrypoint#main(String[])} 执行handler-transformer；
 * ④子类通过 {@link #basedir} 引用临时工作目录，在 @Test 方法中编写断言。
 *
 * @author Deolin 2026-05-13
 */
public abstract class HandlerTransformerItBaseTest {

    /**
     * 临时工作目录（target/it/{caseName}/），子类用此引用做断言
     */
    protected File basedir;

    /**
     * 执行handler-transformer，子类在 @BeforeEach 或 @Test 中调用
     */
    protected void runHandlerTransformer(String caseName) {
        runHandlerTransformer(caseName, null);
    }

    /**
     * 执行handler-transformer（指定domainName），子类在 @BeforeEach 或 @Test 中调用
     */
    protected void runHandlerTransformer(String caseName, String domainName) {
        // 1. 复制测试资源到临时工作目录
        basedir = copyResourceToWorkDir(caseName);

        // 2. 解析yml中的相对路径为绝对路径并回写
        File configFile = resolveAndRewriteConfig(basedir);

        // 3. 组装CLI参数并调用Entrypoint.main()
        List<String> args = new ArrayList<>();
        args.add("--tool=handler-transformer");
        args.add("--config=" + configFile.getAbsolutePath());
        if (domainName != null && !domainName.isEmpty()) {
            args.add("--domain=" + domainName);
        }

        // 保存当前线程的context classloader，Entrypoint→DefaultAstForest会替换它为IT case的URLClassLoader，
        // 导致后续测试中Resources.getResource()找不到allison1875-git.properties等classpath资源
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Entrypoint.main(args.toArray(new String[0]));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            AstForestContext.remove();
            DomainContext.remove();
        }
    }

    /**
     * 将 classpath 下 it/handler-transformer/{caseName}/ 整个目录递归拷贝到 target/it/{caseName}/
     */
    private File copyResourceToWorkDir(String caseName) {
        String resourcePrefix = "it/handler-transformer/" + caseName;
        // 通过ClassLoader获取资源目录的物理路径
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePrefix);
        if (resourceUrl == null) {
            throw new RuntimeException("测试资源目录不存在: " + resourcePrefix);
        }

        Path sourcePath = Paths.get(resourceUrl.getPath());
        Path targetPath = Paths.get("target", "it", caseName);

        try {
            // 清理旧的临时目录
            if (Files.exists(targetPath)) {
                deleteRecursively(targetPath);
            }
            // 递归拷贝
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
     * 读取 .allison1875.yml（以Map模式），将所有相对路径解析为绝对路径后回写到磁盘。
     * 回写后的yml可以直接被 Entrypoint.main() 加载并正确执行。
     *
     * <p>使用Map模式加载和回写，避免 SnakeYAML 的 JavaBean 序列化问题（类型标签、File无参构造器等）。
     *
     * <p>与 DocAnalyzerItBaseTest 的区别：handler-transformer 没有 markdownDir/dslDir/dependencyDirsOrJavaFilePath
     * 等额外路径字段需要解析，只需处理 DomainConfig 中的 *Module 字段。
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

        // 解析DomainConfig中的*Module字段为绝对路径
        List<Map<String, Object>> domains = (List<Map<String, Object>>) configMap.get("domains");
        if (domains != null) {
            String[] moduleKeys = {"controllerModule", "dtoModule", "enumModule", "serviceModule", "serviceImplModule",
                    "persistenceModule"};
            for (Map<String, Object> domain : domains) {
                for (String key : moduleKeys) {
                    String value = (String) domain.get(key);
                    domain.put(key, resolveModuleAbsolutePath(basedirAbsPath, value));
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

    /**
     * 将modulePath解析为绝对路径，与Mojo逻辑一致：null/空/"." → basedir本身
     */
    private String resolveModuleAbsolutePath(String basedirAbsPath, String modulePath) {
        if (modulePath == null || modulePath.isEmpty() || ".".equals(modulePath)) {
            return basedirAbsPath;
        }
        return getCanonicalPath(new File(basedirAbsPath, modulePath));
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
