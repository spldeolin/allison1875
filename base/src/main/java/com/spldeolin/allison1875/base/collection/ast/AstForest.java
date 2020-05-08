package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2020-04-24
 */
@ToString
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    private static final AstForest instance = new AstForest(BaseConfig.getInstace().getProjectPaths());

    private final Collection<Path> projectPaths;

    private AstCursor cursor;

    private AstForest(Collection<Path> projectPaths) {
        this.projectPaths = projectPaths;
        Collection<SourceRoot> sourceRoots = new SourceRootCollector().collect(projectPaths);
        this.cursor = new AstCursor(sourceRoots);
    }

    public static AstForest getInstance() {
        return instance;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cursor;
    }

    public void reset() {
        Collection<SourceRoot> sourceRoots = new SourceRootCollector().collect(projectPaths);
        this.cursor = new AstCursor(sourceRoots);
        log.info("Astforest reset.");
    }

}
