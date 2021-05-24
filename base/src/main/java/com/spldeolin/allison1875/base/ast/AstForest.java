package com.spldeolin.allison1875.base.ast;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    /**
     * Primary Class
     */
    private final Class<?> primaryClass;

    private final boolean wholeProject;

    /**
     * AST森林的根目录
     */
    private final Path astForestRoot;

    /**
     * Primary Class所在Maven Module的src/main/java的路径
     */
    private final Path primaryJavaRoot;

    /**
     * AST森林内的java文件
     */
    private final Set<Path> javasInForest = Sets.newHashSet();

    private AstIterator iterator;

    public AstForest(Class<?> primaryClass, boolean wholeProject) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;

        Path mavenModule = MavenPathResolver.findMavenModule(primaryClass);
        this.primaryJavaRoot = mavenModule.resolve("src/main/java").normalize();

        if (wholeProject) {
            astForestRoot = MavenPathResolver.findMavenProject(primaryClass);
        } else {
            astForestRoot = mavenModule;
        }
        javasInForest.addAll(collectJavas(astForestRoot));
        iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest built [{}]", astForestRoot);
    }

    public AstForest(Class<?> primaryClass, boolean wholeProject, Set<Path> dependencyPaths) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;

        Path mavenModule = MavenPathResolver.findMavenModule(primaryClass);
        this.primaryJavaRoot = mavenModule.resolve("src/main/java").normalize();

        if (wholeProject) {
            astForestRoot = MavenPathResolver.findMavenProject(primaryClass);
        } else {
            astForestRoot = MavenPathResolver.findMavenModule(primaryClass);
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
        return this.iterator;
    }

    public AstForest reset() {
        this.iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest reset");
        return this;
    }

    public AstForest clone() {
        AstForest result = new AstForest(primaryClass, wholeProject);
        log.info("AST Forest cloned");
        return result;
    }

    public Class<?> getPrimaryClass() {
        return primaryClass;
    }

    public Path getAstForestRoot() {
        return astForestRoot;
    }

    public Path getPrimaryJavaRoot() {
        return primaryJavaRoot;
    }

    public Set<Path> getJavasInForest() {
        return javasInForest;
    }

    private Set<Path> collectJavas(Path directory) {
        Set<Path> javaPaths = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{"java"}, true)
                .forEachRemaining(javaFile -> javaPaths.add(javaFile.toPath()));

        int javaCount = javaPaths.size();
        log.info("collect {} of {} from directory [{}]", javaCount, English.plural("java file", javaCount), directory);
        return javaPaths;
    }

}