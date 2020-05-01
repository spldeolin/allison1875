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

    private static final AstForest instance = new AstForest(BaseConfig.getInstace().getProjectModulesMap().keySet());

    private final Collection<Path> sourceRoots;

    private final AstCursorBuffer buffer;

    private final AstCursor cursor;

    private AstForest(Collection<Path> sourceRoots) {
        this.sourceRoots = sourceRoots;
        this.buffer = new AstCursorBuffer(sourceRoots);
        this.cursor = new AstCursor(buffer);
    }

    public static AstForest getInstance() {
        return instance;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cursor;
    }

    public Collection<Path> getSourceRoots() {
        return sourceRoots;
    }

    public AstCursorBuffer getBuffer() {
        return buffer;
    }

    public void reset() {
        cursor.reset();
    }

}
