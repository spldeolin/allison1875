package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.allison1875.base.classloader.MavenProjectClassLoaderFactory;
import lombok.Data;

/**
 * 抽象语法树迭代子
 *
 * @author Deolin 2020-04-23
 */
class AstCursor implements Iterator<CompilationUnit> {

    private final CollectionStrategy collectionStrategy = new ClassLoaderCollectionStrategy(
            MavenProjectClassLoaderFactory.getClassLoader());

    private final CursorBuffer buffer;

    AstCursor(Collection<Path> projectPaths) {
        buffer = new CursorBuffer(projectPaths);
    }

    @Override
    public boolean hasNext() {
        if (buffer.getCompilationUnitIterator().hasNext()) {
            // cus还有
            return true;
        } else if (buffer.getSourceRootIterator().hasNext()) {
            // cus没有了，sourceRoots还有 -> 清空cus，tryToParse下一个sourceRoot，重新收集cus
            buffer.getCompilationUnits().clear();
            buffer.getSourceRootIterator().next().tryToParseParallelized().forEach(parseResult -> {
                if (parseResult.isSuccessful()) {
                    parseResult.getResult().ifPresent(cu -> buffer.getCompilationUnits().add(cu));
                }
            });
            buffer.setCompilationUnitIterator(buffer.getCompilationUnits().iterator());
            // 递归的目的是这个sourceRoot可能没源码
            return hasNext();
        } else if (buffer.getProjectPathIterator().hasNext()) {
            // sourceRoots也没有了，projectPaths还有
            // 清空sourceRoots，collect下一个projectPath，重新收集sourceRoot，重新收集sourceRoots
            buffer.getSourceRoots().clear();
            buffer.getSourceRoots()
                    .addAll(collectionStrategy.collect(buffer.getProjectPathIterator().next()).getSourceRoots());
            buffer.setSourceRootIterator(buffer.getSourceRoots().iterator());
            // 递归原因同上
            return hasNext();
        } else {
            // cus、sourceRoots、projectPaths都没有了，递归结束
            return false;
        }
    }

    public void reset() {
        buffer.setProjectPathIterator(buffer.getProjectPaths().iterator());
        buffer.getSourceRoots().clear();
        buffer.setSourceRootIterator(buffer.getSourceRoots().iterator());
        buffer.getSourceRoots().clear();
        buffer.setCompilationUnitIterator(buffer.getCompilationUnits().iterator());
    }

    @Override
    public CompilationUnit next() {
        return buffer.getCompilationUnitIterator().next();
    }

    @Data
    private static class CursorBuffer {

        private final Collection<Path> projectPaths;

        private Iterator<Path> projectPathIterator;

        private final Collection<SourceRoot> sourceRoots = Lists.newLinkedList();

        private Iterator<SourceRoot> sourceRootIterator = sourceRoots.iterator();

        private final Collection<CompilationUnit> compilationUnits = Lists.newLinkedList();

        private Iterator<CompilationUnit> compilationUnitIterator = compilationUnits.iterator();

        public CursorBuffer(Collection<Path> projectPath) {
            this.projectPaths = projectPath;
            projectPathIterator = projectPaths.iterator();
        }

    }

}
