package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.common.service.AstFilterService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.FileTraverseUtils;
import com.spldeolin.allison1875.common.util.MavenUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2021-02-02
 */
@Log4j2
@Getter
public class AstForest implements Iterable<CompilationUnit> {

    /**
     * Primary Class
     */
    private final Class<?> primaryClass;

    private final boolean wholeProject;

    private final AstFilterService astFilterService;

    /**
     * Primary Class所在Maven Module的src/main/java的路径
     */
    private final Path primaryJavaRoot;

    /**
     * AST森林的根目录
     */
    private final Path astForestRoot;

    /**
     * AST森林内的java文件
     */
    private final Set<File> javasInForest = Sets.newLinkedHashSet();

    private AstIterator iterator;

    public AstForest(Class<?> primaryClass, boolean wholeProject, AstFilterService astFilterService) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;
        this.astFilterService = astFilterService;

        Path mavenModule = MavenUtils.findMavenModule(primaryClass);
        this.primaryJavaRoot = mavenModule.resolve("src/main/java").normalize();

        if (wholeProject) {
            astForestRoot = MavenUtils.findMavenProject(primaryClass);
        } else {
            astForestRoot = mavenModule;
        }
        javasInForest.addAll(collectJavas(astForestRoot));
        iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest built [{}]", astForestRoot);
    }

    public AstForest(Class<?> primaryClass, boolean wholeProject, Set<Path> dependencyPaths,
            AstFilterService astFilterService) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;
        this.astFilterService = astFilterService;

        Path mavenModule = MavenUtils.findMavenModule(primaryClass);
        this.primaryJavaRoot = mavenModule.resolve("src/main/java").normalize();

        if (wholeProject) {
            astForestRoot = MavenUtils.findMavenProject(primaryClass);
        } else {
            astForestRoot = MavenUtils.findMavenModule(primaryClass);
        }
        javasInForest.addAll(collectJavas(astForestRoot));
        for (Path dependencyPath : dependencyPaths) {
            javasInForest.addAll(collectJavas(dependencyPath));
        }
        iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest built [{}]", astForestRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return Iterators.filter(iterator, astFilterService::accept);
    }

    public AstForest reset() {
        this.iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest reset");
        return this;
    }

    public AstForest clone() {
        AstForest result = new AstForest(primaryClass, wholeProject, astFilterService);
        log.info("AST Forest cloned");
        return result;
    }

    private Set<File> collectJavas(Path directory) {
        Set<File> javaPaths = FileTraverseUtils.listFilesRecursively(directory, "java", astFilterService::accept);
        int javaCount = javaPaths.size();
        log.info("collect {} of {} from directory [{}]", javaCount, English.plural("java file", javaCount), directory);
        return javaPaths;
    }

    public CompilationUnit findCu(String primaryTypeQualifier) {
        Path absPath;
        try {
            absPath = getPrimaryJavaRoot().resolve(primaryTypeQualifier.replace('.', File.separatorChar) + ".java");
        } catch (Exception e) {
            log.info("impossible path, qualifier={}", primaryTypeQualifier, e);
            return null;
        }
        CompilationUnit result = CompilationUnitUtils.parseCu(absPath);
        return result;
    }

}