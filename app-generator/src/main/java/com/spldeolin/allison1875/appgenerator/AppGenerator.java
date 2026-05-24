package com.spldeolin.allison1875.appgenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.dsl.AppDef;
import com.spldeolin.allison1875.appgenerator.dsl.MenuDef;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-05-24
 */
@Singleton
@Slf4j
public class AppGenerator implements Allison1875MainService {

    @Inject
    private Config config;

    @Override
    public void process() {
        // 1. Parse app.yml
        AppDef appDef = parseAppDef();
        log.info("parsed AppDef: name={}, title={}, menus={}", appDef.getName(), appDef.getTitle(),
                appDef.getMenus().size());

        // 2. Determine output paths
        String appName = appDef.getName();
        Path outputRoot = config.getAppGeneratorOutputDir().toPath().resolve(appName);
        Path backendOutput = outputRoot.resolve(appName + "-backend");
        Path frontendOutput = outputRoot.resolve(appName + "-frontend");

        // 3. Generate backend
        generateBackend(appDef, backendOutput);

        // 4. Generate frontend
        generateFrontend(appDef, frontendOutput);

        // 5. Generate README.md
        generateReadme(appDef, outputRoot);

        log.info("app-generator completed. output={}", outputRoot.toAbsolutePath());
    }

    private AppDef parseAppDef() {
        try {
            String content = Files.readString(config.getAppDslPath().toPath(), StandardCharsets.UTF_8);
            return new YAMLMapper().readValue(content, AppDef.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void generateBackend(AppDef appDef, Path output) {
        // Copy skeleton from classpath resources
        Path skeleton = getSkeletonPath("backend-skeleton");
        copyDirectory(skeleton, output);

        // Replace placeholders in all files
        String namespace = appDef.getNamespace();
        String namespacePath = namespace.replace('.', '/');
        replaceInAllFiles(output, "__NAMESPACE__", namespace);
        replaceInAllFiles(output, "__NAMESPACE_PATH__", namespacePath);
        replaceInAllFiles(output, "__APP_NAME__", appDef.getName());
        replaceInAllFiles(output, "__APP_TITLE__", appDef.getTitle());

        // Rename __NAMESPACE_PATH__ directory to actual namespace path
        Path placeholderDir = output.resolve("src/main/java/__NAMESPACE_PATH__");
        Path actualDir = output.resolve("src/main/java/" + namespacePath);
        try {
            Files.createDirectories(actualDir.getParent());
            Files.move(placeholderDir, actualDir);
            deleteEmptyParents(placeholderDir.getParent(), output.resolve("src/main/java"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Extract FormDefs for form-generator (to be wired in Task 5)
        List<FormDef> forms = appDef.getMenus().stream()
                .map(MenuDef::getForm)
                .collect(Collectors.toList());
        log.info("extracted {} forms for form-generator delegation", forms.size());
    }

    private void generateFrontend(AppDef appDef, Path output) {
        // Copy skeleton from classpath resources
        Path skeleton = getSkeletonPath("frontend-skeleton");
        copyDirectory(skeleton, output);

        // Write app.json with the full AppDef
        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            String appJson = mapper.writeValueAsString(appDef);
            Files.writeString(output.resolve("src/app.json"), appJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void generateReadme(AppDef appDef, Path outputRoot) {
        String name = appDef.getName();
        try {
            String originalDsl = Files.readString(config.getAppDslPath().toPath(), StandardCharsets.UTF_8);
            String readme = "# " + appDef.getTitle() + "\n\n"
                    + "## 构建\n\n"
                    + "```bash\n"
                    + "cd " + name + "-frontend\n"
                    + "npm install\n"
                    + "npm run build\n"
                    + "cp -r dist/* ../" + name + "-backend/src/main/resources/static/\n"
                    + "cd ../" + name + "-backend\n"
                    + "mvn package\n"
                    + "```\n\n"
                    + "## 运行\n\n"
                    + "```bash\n"
                    + "java -jar " + name + "-backend/target/" + name + "-fullstack.jar\n"
                    + "```\n\n"
                    + "## DSL\n\n"
                    + "```yaml\n"
                    + originalDsl
                    + "\n```\n";
            Files.createDirectories(outputRoot);
            Files.writeString(outputRoot.resolve("README.md"), readme, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getSkeletonPath(String resourceName) {
        try {
            var url = getClass().getClassLoader().getResource(resourceName);
            if (url == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate skeleton resource: " + resourceName, e);
        }
    }

    private void copyDirectory(Path source, Path target) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void replaceInAllFiles(Path dir, String placeholder, String replacement) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && !file.getFileName().toString().equals(".gitkeep")) {
                        String content = Files.readString(file, StandardCharsets.UTF_8);
                        if (content.contains(placeholder)) {
                            Files.writeString(file, content.replace(placeholder, replacement), StandardCharsets.UTF_8);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void deleteEmptyParents(Path dir, Path stopAt) throws IOException {
        while (dir != null && !dir.equals(stopAt) && Files.isDirectory(dir)) {
            try (Stream<Path> entries = Files.list(dir)) {
                if (entries.findAny().isEmpty()) {
                    Files.delete(dir);
                    dir = dir.getParent();
                } else {
                    break;
                }
            }
        }
    }

}
