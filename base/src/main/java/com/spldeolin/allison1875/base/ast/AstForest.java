package com.spldeolin.allison1875.base.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    private final Class<?> primaryClass;

    private final Set<Path> javaPaths = Sets.newHashSet();

    private final Path commonPath;

    private AstIterator iterator;

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
        javaPaths.addAll(collectJavas(root));
        commonPath = calcCommonPath(javaPaths);
        iterator = new AstIterator(primaryClass.getClassLoader(), javaPaths);
        log.info("AST Forest set up");
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
        javaPaths.addAll(collectJavas(root));
        for (Path dependencyPath : dependencyPaths) {
            javaPaths.addAll(collectJavas(dependencyPath));
        }
        commonPath = calcCommonPath(javaPaths);
        iterator = new AstIterator(primaryClass.getClassLoader(), javaPaths);
        log.info("AST Forest set up");
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return this.iterator;
    }

    public AstForest reset() {
        this.iterator = new AstIterator(primaryClass.getClassLoader(), javaPaths);
        log.info("AST Forest reset");
        return this;
    }

    public Class<?> getPrimaryClass() {
        return primaryClass;
    }

    public Path getCommonPath() {
        return commonPath;
    }

    private Set<Path> collectJavas(Path directory) {
        Set<Path> javaPaths = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(directory.toFile(), new String[]{"java"}, true)
                .forEachRemaining(javaFile -> javaPaths.add(javaFile.toPath()));

        int javaCount = javaPaths.size();
        log.info("collect {} of {} from directory [{}]", javaCount, English.plural("java file", javaCount), directory);
        return javaPaths;
    }

    private Path calcCommonPath(Collection<Path> sourceRootPaths) {
        List<Path> paths = Lists.newArrayList(sourceRootPaths);
        String common = paths.get(0).toString();
        for (Path path : paths) {
            common = Strings.commonPrefix(common, path.toString());
        }
        return Paths.get(common);
    }

}