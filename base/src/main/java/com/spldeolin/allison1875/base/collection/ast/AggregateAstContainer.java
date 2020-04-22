package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.BaseConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聚合的抽象语法森林
 *
 * @author Deolin 2020/04/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AggregateAstContainer extends AstContainer {

    private static final AggregateAstContainer instance = new AggregateAstContainer(BaseConfig.getInstace()
        .getProjectPaths());

    private final Collection<Path> projectPaths;

    private final Map<Path, AstContainer> astContainers;

    private final Map<CompilationUnit, Path> projectPathsByCu;

    AggregateAstContainer(Collection<Path> projectPaths) {
        super();
        this.projectPaths = projectPaths;
        astContainers = Maps.newHashMap();
        projectPathsByCu = Maps.newHashMap();
        super.cus = Lists.newLinkedList();
        projectPaths.forEach(path -> {
            AstContainer astContainer = new AstContainer(path);
            astContainers.put(path, astContainer);
            astContainer.getCompilationUnits()
                .forEach(cu -> {
                    super.cus.add(cu);
                    projectPathsByCu.put(cu, path);
                });
            super.cus.addAll(astContainer.getCompilationUnits());
        });
    }

    public static AggregateAstContainer getInstance() {
        return instance;
    }

    @Override
    public Path getPath() {
        throw new UnsupportedOperationException("Paths was aggregated, getPath() doesn't make sense.");
    }

    public Collection<Path> getProjectPaths() {
        return projectPaths;
    }

    public AstContainer getAstContainer(Path path) {
        return astContainers.get(path);
    }

    public Path getProjectPath(CompilationUnit compilationUnit) {
        return projectPathsByCu.get(compilationUnit);
    }

}
