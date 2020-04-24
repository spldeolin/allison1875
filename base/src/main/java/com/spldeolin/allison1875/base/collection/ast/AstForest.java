package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Getter;
import lombok.ToString;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2020-04-24
 */
@Getter
@ToString
public class AstForest implements Iterable<CompilationUnit> {

    private static final AstForest instance = new AstForest(BaseConfig.getInstace().getProjectPaths());

    private final Collection<Path> projectPaths;

    private final boolean cursorMode;

    private AstCursor astCursor;

    private AggregateAstContainer aggregateAstContainer;

    public AstForest(Collection<Path> projectPaths) {
        this(projectPaths, projectPaths.size() > 1);
    }

    public AstForest(Collection<Path> projectPaths, boolean cursorMode) {
        this.projectPaths = projectPaths;
        this.cursorMode = cursorMode;
        if (cursorMode) {
            astCursor = new AstCursor(projectPaths);
        } else {
            aggregateAstContainer = new AggregateAstContainer(projectPaths);
        }
    }

    public static AstForest getInstance() {
        return instance;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        if (cursorMode) {
            return astCursor;
        } else {
            return aggregateAstContainer.getCompilationUnits().iterator();
        }
    }

    public AggregateAstContainer getAggregateAstContainer() {
        if (cursorMode) {
            throw new IllegalStateException("AstForest is not in cursor mode.");
        } else {
            return aggregateAstContainer;
        }
    }

}
