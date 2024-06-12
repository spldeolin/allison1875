package com.spldeolin.allison1875.common.service.impl;

import java.io.File;
import java.nio.file.Path;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.common.util.MavenUtils;

/**
 * <pre>
 *                               SourceRoot
 *                             /
 *                 ModuleRoot < - other Root
 *               /             \
 * ProjectRoot  <                other Root
 *               \
 *                ModuleRoot
 *
 * DependencyProject Root
 *
 * DependencyProject Root
 * </pre>
 *
 * @author Deolin 2024-01-17
 */
@Singleton
public class MavenAstForestResidenceService implements AstForestResidenceService {

    private static final String testClassDirectoryName = "test-classes";

    private static final String mainJavaRelativePath = "/src/main/java";

    private static final String testJavaRelativePath = "/src/test/java";

    @Override
    public File findAstForestRoot(Class<?> primaryClass) {
        Path classLoaderRoot = CodeGenerationUtils.classLoaderRoot(primaryClass);
        if (testClassDirectoryName.equals(classLoaderRoot.toFile().getName())) {
            return new File(MavenUtils.findMavenModule(primaryClass) + testJavaRelativePath);
        } else {
            return new File(MavenUtils.findMavenModule(primaryClass) + mainJavaRelativePath);
        }
    }

    @Override
    public Path findModuleRoot(Class<?> primaryClass) {
        return MavenUtils.findMavenModule(primaryClass).toPath();
    }

}