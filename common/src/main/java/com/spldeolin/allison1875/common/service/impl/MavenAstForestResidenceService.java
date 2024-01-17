package com.spldeolin.allison1875.common.service.impl;

import java.io.File;
import java.nio.file.Path;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.common.util.MavenUtils;

/**
 * @author Deolin 2024-01-17
 */
@Singleton
public class MavenAstForestResidenceService implements AstForestResidenceService {

    private static final String testClassDirectoryName = "test-classes";

    private static final String mainJavaRelativePath = "/src/main/java";

    private static final String testJavaRelativePath = "/src/test/java";

    @Override
    public File findWorkModuleRoot(Class<?> primaryClass) {
        return MavenUtils.findMavenModule(primaryClass);
    }

    @Override
    public File findWorkAstForestRoot(Class<?> primaryClass) {
        Path classLoaderRoot = CodeGenerationUtils.classLoaderRoot(primaryClass);
        if (testClassDirectoryName.equals(classLoaderRoot.toFile().getName())) {
            return new File(this.findWorkModuleRoot(primaryClass).getPath() + testJavaRelativePath);
        } else {
            return new File(this.findWorkModuleRoot(primaryClass).getPath() + mainJavaRelativePath);
        }
    }

}