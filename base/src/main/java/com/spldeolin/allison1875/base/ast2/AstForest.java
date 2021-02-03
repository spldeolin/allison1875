package com.spldeolin.allison1875.base.ast2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    private final Class<?> primaryClass;

    private final AstIterator iterator;

    public AstForest(Class<?> primaryClass, boolean wholeProject) {
        this.primaryClass = primaryClass;
        Path root;
        if (wholeProject) {
            root = MavenPathResolver.findMavenProject(primaryClass);
            log.info("find project path [{}]", root);
        } else {
            root = MavenPathResolver.findMavenModule(primaryClass);
            log.info("find host path [{}]", root);
        }
        Set<Path> javaPaths = collectJavas(root);
        this.iterator = new AstIterator(primaryClass.getClassLoader(), javaPaths);
    }

    public AstForest(Class<?> primaryClass, boolean wholeProject, Set<Path> dependencyPaths) {
        this.primaryClass = primaryClass;
        Path root;
        if (wholeProject) {
            root = MavenPathResolver.findMavenProject(primaryClass);
            log.info("find project path [{}]", root);
        } else {
            root = MavenPathResolver.findMavenModule(primaryClass);
            log.info("find host path [{}]", root);
        }
        Set<Path> javaPaths = collectJavas(root);
        dependencyPaths.forEach(dependencyPath -> javaPaths.addAll(collectJavas(dependencyPath)));
        this.iterator = new AstIterator(primaryClass.getClassLoader(), javaPaths);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return this.iterator;
    }

    private Set<Path> collectJavas(Path directory) {
        Set<Path> javaPaths = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{"java"}, true)
                .forEachRemaining(javaFile -> javaPaths.add(javaFile.toPath()));

        int javaCount = javaPaths.size();
        log.info("collect {} of {} from directory [{}]", javaCount, English.plural("java file", javaCount), directory);
        return javaPaths;
    }

    public static void main(String[] args) {
        Set<Path> dependencies = Sets.newHashSet();
        dependencies.add(Paths.get("/Users/deolin/Documents/project-repo/joyowo"));

        new AstForest(AstForest.class, true, dependencies).forEach(cu -> {
        });
    }

}