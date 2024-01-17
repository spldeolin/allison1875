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
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.exception.CompilationUnitParseException;
import com.spldeolin.allison1875.common.service.AstFilterService;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    @Getter
    private final Class<?> primaryClass;

    private final AstForestResidenceService astForestResidenceService;

    private final AstFilterService astFilterService;

    /**
     * 代表AST森林所处的Module的根目录
     */
    @Getter
    private final Path moduleRoot;

    /**
     * AST森林的根目录
     */
    @Getter
    private final Path astForestRoot;

    public AstForest(Class<?> primaryClass, AstForestResidenceService astForestResidenceService,
            AstFilterService astFilterService) {
        this.primaryClass = primaryClass;
        this.astFilterService = astFilterService;
        this.astForestResidenceService = astForestResidenceService;

        moduleRoot = astForestResidenceService.findWorkModuleRoot(primaryClass).toPath();
        astForestRoot = astForestResidenceService.findWorkAstForestRoot(primaryClass).toPath();

        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(primaryClass.getClassLoader())));
        log.info("AST Forest built [{}]", astForestRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        Iterator<File> javaItr = FileUtils.iterateFiles(astForestRoot.toFile(), new String[]{"java"}, true);
        Iterator<File> filteredJavaItr = Iterators.filter(javaItr, astFilterService::accept);
        Iterator<CompilationUnit> cuItr = Iterators.transform(filteredJavaItr, CompilationUnitUtils::parseJava);
        Iterator<CompilationUnit> filteredCuItr = Iterators.filter(cuItr, astFilterService::accept);
        return filteredCuItr;
    }

    public AstForest cloneWithResetting() {
        AstForest result = new AstForest(primaryClass, astForestResidenceService, astFilterService);
        log.info("AST Forest cloned");
        return result;
    }

    public Optional<CompilationUnit> findCu(String primaryTypeQualifier) {
        Path absPath;
        try {
            absPath = astForestRoot.resolve(qualifierToRelativePath(primaryTypeQualifier));
        } catch (Exception e) {
            log.warn("impossible path, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        CompilationUnit cu;
        try {
            cu = CompilationUnitUtils.parseJava(absPath.toFile());
        } catch (CompilationUnitParseException e) {
            log.warn("fail to find cu, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
        if (!astFilterService.accept(cu)) {
            return Optional.empty();
        }
        return Optional.of(cu);
    }

    private String qualifierToRelativePath(String qualifier) {
        return qualifier.replace('.', File.separatorChar) + ".java";
    }

}