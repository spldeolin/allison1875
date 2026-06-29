package com.spldeolin.allison1875.appgenerator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.appgenerator.dsl.AppDef;
import com.spldeolin.allison1875.appgenerator.dsl.MenuDef;
import com.spldeolin.allison1875.appgenerator.service.AuditOperationTypeEnumGenerateService;
import com.spldeolin.allison1875.appgenerator.service.ControllerAuthAnnotateService;
import com.spldeolin.allison1875.appgenerator.service.PermissionEnumGenerateService;
import com.spldeolin.allison1875.appgenerator.service.impl.AppGeneratorCommonItemsExpansionServiceImpl;
import com.spldeolin.allison1875.appgenerator.service.impl.AppGeneratorMutationExpansionServiceImpl;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.DefaultAstForest;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils;
import com.spldeolin.allison1875.common.util.MavenUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.FormGenerator;
import com.spldeolin.allison1875.formgenerator.FormGeneratorModule;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-05-24
 */
@Singleton
@Slf4j
public class AppGenerator implements Allison1875Game {

    @Inject
    private Config config;

    @Inject
    private PermissionEnumGenerateService permissionEnumGenerateService;

    @Inject
    private AuditOperationTypeEnumGenerateService auditOperationTypeEnumGenerateService;

    @Inject
    private ControllerAuthAnnotateService controllerAuthAnnotateService;

    @Override
    public void play() {
        // 1. Parse app.yml
        AppDef appDef = parseAppDef();
        appDef.validate();
        log.info("parsed AppDef: name={}, title={}, menus={}", appDef.getName(), appDef.getTitle(),
                appDef.getMenus().size());

        // 2. Determine output paths
        String appName = appDef.getName();
        Path outputRoot = determineOutputRoot(config.getAppGeneratorOutputDir().toPath(), appName);
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

    private Path determineOutputRoot(Path baseDir, String appName) {
        Path candidate = baseDir.resolve(appName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        int seq = 1;
        Path candidateWithSeq;
        do {
            candidateWithSeq = baseDir.resolve(appName + "-" + seq);
            seq++;
        } while (Files.exists(candidateWithSeq));
        return candidateWithSeq;
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
        replaceInAllFiles(output, "__DATASOURCE_URL__", config.getJdbcUrl());
        replaceInAllFiles(output, "__DATASOURCE_SCHEMA__", config.getSchema());
        replaceInAllFiles(output, "__DATASOURCE_USERNAME__", config.getUserName());
        replaceInAllFiles(output, "__DATASOURCE_PASSWORD__", config.getPassword());

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

        // Generate permission enum (fills the skeleton's empty PermissionEnum shell)
        List<FormDef> allForms = Lists.newArrayList(appDef.getMenus().stream()
                .map(MenuDef::getForm).collect(Collectors.toList()));
        permissionEnumGenerateService.generatePermissionEnum(allForms, output, appDef.getNamespace());

        // Generate audit operation type enum if AuditLog builtin form is present
        boolean hasAuditLogForm = parseBuiltinMenus().stream()
                .anyMatch(menu -> "AuditLog".equals(menu.getForm().getName()));
        if (hasAuditLogForm) {
            auditOperationTypeEnumGenerateService.generateAuditOperationTypeEnum(allForms, output,
                    appDef.getNamespace());
        }

        // Extract FormDefs for form-generator (to be wired in Task 5)
        List<FormDef> forms = appDef.getMenus().stream().map(MenuDef::getForm).collect(Collectors.toList());
        log.info("extracted {} forms for form-generator delegation", forms.size());

        // Delegate to form-generator for CRUD code generation
        invokeFormGenerator(appDef, output, forms);

        // Annotate generated controllers with @WebApiAuth
        controllerAuthAnnotateService.annotateControllers(forms, output, appDef.getNamespace());
    }

    private void generateFrontend(AppDef appDef, Path output) {
        // Copy skeleton from classpath resources
        Path skeleton = getSkeletonPath("frontend-skeleton");
        copyDirectory(skeleton, output);

        // Merge builtin-form.yml menus into user DSL menus
        List<MenuDef> mergedMenus = Lists.newArrayList(appDef.getMenus());
        List<MenuDef> builtinMenus = parseBuiltinMenus();
        mergedMenus.addAll(builtinMenus);
        log.info("merged {} builtin menus into frontend app.json", builtinMenus.size());

        // Force builtin menus to sort after all user-defined menus
        for (int i = 0; i < mergedMenus.size(); i++) {
            MenuDef menu = mergedMenus.get(i);
            if (builtinMenus.contains(menu)) {
                mergedMenus.set(i, menu.toBuilder()
                        .order(100000 + (menu.getOrder() != null ? menu.getOrder() : 0))
                        .build());
            }
        }

        // Write app.json with merged menus
        try {
            // Inject permissions into each menu
            for (int i = 0; i < mergedMenus.size(); i++) {
                MenuDef menu = mergedMenus.get(i);
                String upperSnake = MoreStringUtils.camelToSnakeCase(menu.getForm().getName()).toUpperCase();
                mergedMenus.set(i, menu.toBuilder()
                        .permissions(MenuDef.Permissions.builder()
                                .list("LIST_" + upperSnake)
                                .create("CREATE_" + upperSnake)
                                .update("UPDATE_" + upperSnake)
                                .delete("DELETE_" + upperSnake)
                                .build())
                        .build());
            }

            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            AppDef frontendAppDef = AppDef.builder()
                    .namespace(appDef.getNamespace())
                    .name(appDef.getName())
                    .title(appDef.getTitle())
                    .menus(mergedMenus)
                    .build();
            String appJson = mapper.writeValueAsString(frontendAppDef);
            Files.writeString(output.resolve("src/app.json"), appJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<MenuDef> parseBuiltinMenus() {
        try {
            var url = getClass().getClassLoader().getResource("frontend-skeleton/src/builtin-form.yml");
            if (url == null) {
                log.warn("builtin-form.yml not found in classpath");
                return Lists.newArrayList();
            }
            String content = Files.readString(Paths.get(url.toURI()), StandardCharsets.UTF_8);
            AppDef builtinDef = new YAMLMapper().readValue(content, AppDef.class);
            return builtinDef.getMenus() != null ? builtinDef.getMenus() : Lists.newArrayList();
        } catch (Exception e) {
            log.warn("failed to parse builtin-form.yml", e);
            return Lists.newArrayList();
        }
    }

    private void generateReadme(AppDef appDef, Path outputRoot) {
        String name = appDef.getName();
        try {
            String originalDsl = Files.readString(config.getAppDslPath().toPath(), StandardCharsets.UTF_8);
            String frontendDir = name + "-frontend";
            String backendDir = name + "-backend";
            String readme = "# " + appDef.getTitle() + "\n\n" + "## Quick Start\n\n" + "```bash\n" + "# 构建前端\n"
                    + "npm ci --prefix " + frontendDir + " && npm run build --prefix " + frontendDir + "\n"
                    + "# 构建后端（含前端产物）\n" + "cp -r " + frontendDir + "/dist/* " + backendDir
                    + "/src/main/resources/static/\n" + "mvn package -T 1C -DskipTests -f " + backendDir + "\n"
                    + "# 运行\n" + "java -jar " + backendDir + "/target/" + name + "-fullstack.jar\n" + "```\n\n"
                    + "## DSL\n\n" + "```yaml\n" + originalDsl + "\n```\n";
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
                    String dirName = dir.getFileName().toString();
                    // Skip directories that should not be copied
                    if (dirName.equals("node_modules") || dirName.equals(".git") || dirName.equals("dist")
                            || dirName.equals(".gitkeep") || dirName.equals("target")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
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
        Path tempDsl = writeTempFormsDsl(forms);
        String absPath = backendOutput.toAbsolutePath().toString();
        String ns = appDef.getNamespace();

        Config.CodeSnippet cs = Config.CodeSnippet.builder()
                .requestResultQualifier(ns + ".common.RequestResult")
                .requestResultTypeDeclaration("RequestResult<${dataType}>")
                .requestResultSuccessNoData("RequestResult.success()")
                .requestResultSuccessWithData("RequestResult.success(${data})")
                .bizExceptionQualifier(ns + ".common.BizException")
                .controllerRequestMapping("/api/v1/${formName}")
                .shortUuidGeneration("UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()")
                .collectionEmptyCheck("${list} == null || ${list}.isEmpty()")
                .build();

        DomainConfig dc = DomainConfig.builder()
                .name("default")
                .controllerModule(absPath)
                .controllerPackage(ns + ".controller")
                .dtoModule(absPath)
                .reqDTOPackage(ns + ".dto.req")
                .respDTOPackage(ns + ".dto.resp")
                .enumModule(absPath)
                .enumPackage(ns + ".enums")
                .serviceModule(absPath)
                .servicePackage(ns + ".service")
                .serviceImplModule(absPath)
                .serviceImplPackage(ns + ".service.impl")
                .persistenceModule(absPath)
                .mapperPackage(ns + ".mapper")
                .entityPackage(ns + ".entity")
                .designPackage(ns + ".design")
                .paramDTOPackage(ns + ".dto.param")
                .recordDTOPackage(ns + ".dto.record")
                .wholeDTOPackage(ns + ".dto")
                .mapperXmlDirs(List.of(new File("src/main/resources/mapper")))
                .build();

        Config fgConfig = Config.builder()
                .dslPath(tempDsl.toFile())
                .author(config.getAuthor())
                .enableGenerateDesign(true)
                .isEntityEndWithEntity(true)
                .enableJavaxMoveToJakarta(false)
                .enableOneService(true)
                .markdownDir(new File(absPath + "/api-docs"))
                .codeSnippet(cs)
                .domains(Lists.newArrayList(dc))
                .isDataModelWithoutLombok(false)
                .enableNoModifyAnnounce(true)
                .tables(new ArrayList<>())
                .dependencyDirsOrJavaFilePath(new ArrayList<>())
                .globalUrlPrefix("")
                .flushTo(List.of(FlushToEnum.MARKDOWN))
                .singleEndpointPerMarkdown(false)
                .getEnumCodeMethodName("getCode")
                .getEnumTitleMethodName("getTitle")
                .wholeDTONamePostfix("WholeDTO")
                .docAnalyzerModule("com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule")
                .handlerTransformerModule("com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule")
                .persistenceGeneratorModule("com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule")
                .queryTransformerModule("com.spldeolin.allison1875.querytransformer.QueryTransformerModule")
                .starTransformerModule("com.spldeolin.allison1875.startransformer.StarTransformerModule")
                .formGeneratorModule("com.spldeolin.allison1875.formgenerator.FormGeneratorModule")
                .appGeneratorModule("com.spldeolin.allison1875.appgenerator.AppGeneratorModule")
                .build();

        AstForest astForest = new DefaultAstForest(MavenUtils.buildClassLoader(new File(absPath), null),
                new File(absPath));
        AstForestContext.set(astForest);

        Allison1875.prepareDomain(fgConfig, null);

        Module fgModule = new FormGeneratorModule(fgConfig);
        List<MenuDef> builtinMenusForFg = parseBuiltinMenus();
        boolean hasUserForm = builtinMenusForFg.stream()
                .anyMatch(menu -> "User".equals(menu.getForm().getName()));
        boolean hasAuditLogForm = builtinMenusForFg.stream()
                .anyMatch(menu -> "AuditLog".equals(menu.getForm().getName()));
        Module combined;
        if (hasUserForm) {
            Module expansionOverride = new AbstractModule() {
                @Override
                protected void configure() {
                    bind(CommonItemsExpansionService.class)
                            .toInstance(new AppGeneratorCommonItemsExpansionServiceImpl());
                    bind(MutationExpansionService.class)
                            .toInstance(new AppGeneratorMutationExpansionServiceImpl(ns, hasAuditLogForm));
                }
            };
            combined = Modules.override(fgModule).with(expansionOverride);
        } else {
            combined = fgModule;
        }

        log.info("invoking form-generator for {} forms...", forms.size());
        Injector injector = Guice.createInjector(combined);
        injector.getInstance(FormGenerator.class).play();
        log.info("form-generator completed");

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
