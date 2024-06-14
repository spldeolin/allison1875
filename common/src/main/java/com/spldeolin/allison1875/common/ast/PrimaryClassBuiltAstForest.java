package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.exception.CompilationUnitParseException;
import com.spldeolin.allison1875.common.service.AstFilterService;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2021-02-02
 */
@Slf4j
public final class PrimaryClassBuiltAstForest implements AstForest {

    private final Class<?> primaryClass;

    private final AstForestResidenceService astForestResidenceService;

    private final AstFilterService astFilterService;

    private final Path sourceRoot;

    public PrimaryClassBuiltAstForest(Class<?> primaryClass, AstForestResidenceService astForestResidenceService,
            AstFilterService astFilterService) {
        Preconditions.checkNotNull(primaryClass, "required Argument 'primaryClass' must not be null");
        Preconditions.checkNotNull(astForestResidenceService,
                "required Argument 'astForestResidenceService' must not be null");

        this.primaryClass = primaryClass;
        this.astFilterService = astFilterService;
        this.astForestResidenceService = astForestResidenceService;

        sourceRoot = astForestResidenceService.findAstForestRoot(primaryClass).toPath();

        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(primaryClass.getClassLoader())));
        Thread.currentThread().setContextClassLoader(primaryClass.getClassLoader());
        log.info("AST Forest built [{}]", sourceRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        // java files
        Iterator<File> javaFilesItr = FileUtils.iterateFiles(sourceRoot.toFile(), new String[]{"java"}, true);
        // filtered java files
        if (astFilterService != null) {
            javaFilesItr = Iterators.filter(javaFilesItr, astFilterService::accept);
        }
        // cus
        Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
        // filtered cus
        if (astFilterService != null) {
            cusItr = Iterators.filter(cusItr, astFilterService::accept);
        }
        return cusItr;
    }

    @Override
    public AstForest cloneWithResetting() {
        PrimaryClassBuiltAstForest result = new PrimaryClassBuiltAstForest(primaryClass, astForestResidenceService,
                astFilterService);
        log.info("AST Forest cloned");
        return result;
    }

    @Override
    public ClassLoader getClassLoader() {
        return primaryClass.getClassLoader();
    }

    @Override
    public Path getSourceRoot() {
        return sourceRoot;
    }

    @Override
    public Optional<CompilationUnit> findCu(String primaryTypeQualifier) {
        Path absPath;
        try {
            absPath = sourceRoot.resolve(qualifierToRelativePath(primaryTypeQualifier));
        } catch (Exception e) {
            log.warn("impossible path, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        if (!absPath.toFile().exists()) {
            return Optional.empty();
        }
        CompilationUnit cu;
        try {
            cu = CompilationUnitUtils.parseJava(absPath.toFile());
        } catch (CompilationUnitParseException e) {
            log.warn("fail to parse cu, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        if (astFilterService != null && !astFilterService.accept(cu)) {
            return Optional.empty();
        }
        return Optional.of(cu);
    }

    @Override
    public Optional<File> resolve(String relativePathOrAbsolutePath) {
        File file = sourceRoot.resolve(relativePathOrAbsolutePath).toFile();
        if (file.exists()) {
            return Optional.of(file);
        }
        return Optional.empty();
    }

    private String qualifierToRelativePath(String qualifier) {
        return qualifier.replace('.', File.separatorChar) + ".java";
    }

}