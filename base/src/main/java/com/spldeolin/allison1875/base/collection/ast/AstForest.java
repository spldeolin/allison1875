package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2020-04-24
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    private static final AstForest instance = new AstForest(stringToPath(BaseConfig.getInstance().getProjectPaths()));

    private AstCursor cursor;

    private AstForest(Collection<Path> projectPaths) {
        Collection<SourceRoot> sourceRoots = new SourceRootCollector().collect(projectPaths);
        this.cursor = new AstCursor(sourceRoots);
    }

    private static Collection<Path> stringToPath(Collection<String> paths) {
        return paths.stream().map(Paths::get).collect(Collectors.toList());
    }

    public static AstForest getInstance() {
        return instance;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cursor;
    }

    public AstForest reset() {
        log.info("Astforest reset.");
        Collection<SourceRoot> sourceRoots = new SourceRootCollector()
                .collect(stringToPath(BaseConfig.getInstance().getProjectPaths()));
        this.cursor = new AstCursor(sourceRoots);
        return this;
    }

}
