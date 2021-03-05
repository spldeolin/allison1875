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
     * AST森林内的java文件
     */
    private final Set<Path> javasInForest = Sets.newHashSet();

    /**
     * 所有javasInForest的共同路径前缀
     */
    private final Path commonPath;

    private AstIterator iterator;

    public AstForest(Class<?> primaryClass, boolean wholeProject) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;
        if (wholeProject) {
            astForestRoot = MavenPathResolver.findMavenProject(primaryClass);
        } else {
            astForestRoot = MavenPathResolver.findMavenModule(primaryClass);
        }
        javasInForest.addAll(collectJavas(astForestRoot));
        commonPath = calcCommonPath(javasInForest);
        iterator = new AstIterator(primaryClass.getClassLoader(), javasInForest);
        log.info("AST Forest built [{}]", astForestRoot);
    }

    public AstForest(Class<?> primaryClass, boolean wholeProject, Set<Path> dependencyPaths) {
        this.primaryClass = primaryClass;
        this.wholeProject = wholeProject;
        if (wholeProject) {
            astForestRoot = MavenPathResolver.findMavenProject(primaryClass);
        } else {
            astForestRoot = MavenPathResolver.findMavenModule(primaryClass);
        }
        javasInForest.addAll(collectJavas(astForestRoot));
        for (Path dependencyPath : dependencyPaths) {
            javasInForest.addAll(collectJavas(dependencyPath));
        }
        commonPath = calcCommonPath(javasInForest);
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

    public Path getCommonPath() {
        return commonPath;
    }

    public Path getAstForestRoot() {
        return astForestRoot;
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