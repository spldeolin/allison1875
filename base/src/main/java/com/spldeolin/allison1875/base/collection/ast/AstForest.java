package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.ToString;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2020-04-24
 */
@ToString
public class AstForest implements Iterable<CompilationUnit> {

    private static final AstForest instance = new AstForest(BaseConfig.getInstace().getProjectPaths());

    private final AstCursorBuffer buffer;

    private final AstCursor cursor;

    private AstForest(Collection<Path> projectPaths) {
        this.buffer = new AstCursorBuffer(new SourceRootCollector().collect(projectPaths));
        this.cursor = new AstCursor(buffer);
    }

    public static AstForest getInstance() {
        return instance;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cursor;
    }

    public AstCursorBuffer getBuffer() {
        return buffer;
    }

    public void reset() {
        cursor.reset();
    }

}
