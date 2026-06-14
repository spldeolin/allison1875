package com.spldeolin.allison1875.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;

class MavenUtilsTest {

    private static Path tempDir;
    private static File singleModuleDir;
    private static File multiModuleDir;
    private static File moduleBDir;

    @BeforeAll
    static void setUp() throws IOException {
        tempDir = Files.createTempDirectory("maven-utils-test-");

        // Copy single-module project to temp dir
        Path singleModuleSrc = Path.of("src/test/resources/maven-test-project");
        singleModuleDir = tempDir.resolve("maven-test-project").toFile();
        copyDirectory(singleModuleSrc, singleModuleDir.toPath());

        // Copy multi-module project to temp dir
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
    void compile_noPomDir_throwsException() {
        File noPomDir = tempDir.toFile();
        assertThrows(Allison1875Exception.class, () -> MavenUtils.compile(noPomDir, null));
    }

    @Test
    void buildClassLoader_singleModule_returnsWorkingClassLoader() throws Exception {
        ClassLoader cl = MavenUtils.buildClassLoader(singleModuleDir, null);
        assertNotNull(cl);
        Class<?> stringUtilsClass = cl.loadClass("org.apache.commons.lang3.StringUtils");
        assertNotNull(stringUtilsClass);
    }

    @Test
    void buildClassLoader_includesTargetClasses() throws Exception {
        ClassLoader cl = MavenUtils.buildClassLoader(singleModuleDir, null);
        Class<?> helloClass = cl.loadClass("com.example.Hello");
        assertNotNull(helloClass);
    }

    @Test
    void buildClassLoader_invalidDir_throwsException() {
        File nonExistent = new File("/tmp/non-existent-dir-xyz");
        assertThrows(Allison1875Exception.class, () -> MavenUtils.buildClassLoader(nonExistent, null));
    }

    @Test
    void findTopLevelProjectDir_multiModule_findsRoot() throws Exception {
        java.lang.reflect.Method method = MavenUtils.class.getDeclaredMethod("findTopLevelProjectDir", File.class);
        method.setAccessible(true);
        File result = (File) method.invoke(null, moduleBDir);
        assertEquals(multiModuleDir.getCanonicalPath(), result.getCanonicalPath());
    }

    @Test
    void findTopLevelProjectDir_singleModule_returnsSelf() throws Exception {
        java.lang.reflect.Method method = MavenUtils.class.getDeclaredMethod("findTopLevelProjectDir", File.class);
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
