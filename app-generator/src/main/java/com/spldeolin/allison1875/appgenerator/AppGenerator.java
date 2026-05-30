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
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.dsl.AppDef;
import com.spldeolin.allison1875.appgenerator.dsl.MenuDef;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.enums.PageParamStyleEnum;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils;
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

        try {
            // 3. Generate backend
            generateBackend(appDef, backendOutput);

            // 4. Generate frontend
            generateFrontend(appDef, frontendOutput);

            // 5. Generate README.md
            generateReadme(appDef, outputRoot);
        } catch (Exception e) {
            log.warn("generation failed, deleting outputRoot: {}", outputRoot.toAbsolutePath());
            try {
                if (Files.exists(outputRoot)) {
                    FileSnapshotUtils.deleteDirectory(outputRoot);
                }
            } catch (IOException ex) {
                log.error("failed to delete outputRoot after generation failure", ex);
            }
            throw e;
        }

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

        // Delegate to form-generator for CRUD code generation
        invokeFormGenerator(appDef, output, forms);
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
            String frontendDir = name + "-frontend";
            String backendDir = name + "-backend";
            String readme = "# " + appDef.getTitle() + "\n\n"
                    + "## 构建\n\n"
                    + "```bash\n"
                    + "cd " + frontendDir + " && npm install && npm run build && cd ..\n"
                    + "cp -r " + frontendDir + "/dist/* " + backendDir + "/src/main/resources/static/\n"
                    + "mvn clean package -f " + backendDir + "\n"
                    + "```\n\n"
                    + "## 运行\n\n"
                    + "```bash\n"
                    + "java -jar " + backendDir + "/target/" + name + "-fullstack.jar\n"
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

    private void invokeFormGenerator(AppDef appDef, Path backendOutput, List<FormDef> forms) {
        // Serialize forms to a temp YAML for form-generator to read
        Path tempDsl = writeTempFormsDsl(forms);

        // Construct config for form-generator
        Config fgConfig = new Config();
        fgConfig.setDslPath(tempDsl.toFile());
        fgConfig.setJavaVersion("1.8"); // 当前只有java8的后端骨架
        fgConfig.setAuthor(config.getAuthor());
        fgConfig.setEnableDocAnalyzer(false);
        fgConfig.setJdbcUrl(null);
        fgConfig.setEnableGenerateDesign(true);
        fgConfig.setIsEntityEndWithEntity(true);
        fgConfig.setEnableJavaxMoveToJakarta(false);
        fgConfig.setPageParamStyle(PageParamStyleEnum.PAGE_NO_PAGE_SIZE);

        // Set code snippets for the generated backend
        Config.CodeSnippet cs = new Config.CodeSnippet();
        String ns = appDef.getNamespace();
        cs.setPageTypeQualifier(ns + ".common.PageResult");
        cs.setRequestResultQualifier(ns + ".common.RequestResult");
        cs.setRequestResultTypeDeclaration("RequestResult<${dataType}>");
        cs.setRequestResultSuccessNoData("RequestResult.success()");
        cs.setRequestResultSuccessWithData("RequestResult.success(${data})");
        cs.setConstructPageResult("PageResult.of(${total}, ${dtos})");
        cs.setConstructEmptyPageResult("PageResult.empty()");
        fgConfig.setCodeSnippet(cs);

        // Construct DomainConfig pointing to the generated backend
        String absPath = backendOutput.toAbsolutePath().toString();
        DomainConfig dc = new DomainConfig();
        dc.setName("default");
        dc.setControllerModule(absPath);
        dc.setControllerPackage(ns + ".controller");
        dc.setDtoModule(absPath);
        dc.setReqDTOPackage(ns + ".dto.req");
        dc.setRespDTOPackage(ns + ".dto.resp");
        dc.setEnumModule(absPath);
        dc.setEnumPackage(ns + ".enums");
        dc.setServiceModule(absPath);
        dc.setServicePackage(ns + ".service");
        dc.setServiceImplModule(absPath);
        dc.setServiceImplPackage(ns + ".service.impl");
        dc.setPersistenceModule(absPath);
        dc.setMapperPackage(ns + ".mapper");
        dc.setEntityPackage(ns + ".entity");
        dc.setDesignPackage(ns + ".design");
        dc.setParamDTOPackage(ns + ".mapper");
        dc.setRecordDTOPackage(ns + ".mapper");
        dc.setWholeDTOPackage(ns + ".dto");
        fgConfig.setDomains(Lists.newArrayList(dc));

        // Invoke form-generator via Allison1875 framework
        log.info("invoking form-generator for {} forms...", forms.size());
        Allison1875.letsGo(ToolEnum.FORM_GENERATOR, fgConfig, null);
        log.info("form-generator completed");

        // Cleanup temp file
        try {
            Files.deleteIfExists(tempDsl);
        } catch (IOException ignored) {
        }
    }

    private Path writeTempFormsDsl(List<FormDef> forms) {
        try {
            Path temp = Files.createTempFile("app-generator-forms-", ".yml");
            String yaml = new YAMLMapper().writeValueAsString(forms);
            Files.writeString(temp, yaml, StandardCharsets.UTF_8);
            return temp;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
