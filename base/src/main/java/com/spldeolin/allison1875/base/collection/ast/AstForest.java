package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.BaseConfig.ProjectModule;
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

    private static final AstForest instance = new AstForest(BaseConfig.getInstace().getProjectPaths(),
            BaseConfig.getInstace().getProjectModules());

    private AstCursor cursor;

    private AstForest(Collection<Path> projectPaths, Collection<ProjectModule> projectModules) {
        Collection<SourceRoot> sourceRoots = new SourceRootCollector().collect(projectPaths,
                projectModules.stream().map(ProjectModule::getSourceRootPath).collect(Collectors.toList()));
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
        log.info("Astforest reset.");
        Collection<SourceRoot> sourceRoots = new SourceRootCollector()
                .collect(BaseConfig.getInstace().getProjectPaths(),
                        BaseConfig.getInstace().getProjectModules().stream().map(ProjectModule::getSourceRootPath)
                                .collect(Collectors.toList()));
        this.cursor = new AstCursor(sourceRoots);
    }

}
