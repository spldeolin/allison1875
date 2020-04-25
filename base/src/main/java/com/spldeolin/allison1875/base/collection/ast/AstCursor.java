package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.allison1875.base.classloader.MavenProjectClassLoaderFactory;

/**
 * 抽象语法树迭代子
 *
 * @author Deolin 2020-04-23
 */
class AstCursor implements Iterator<CompilationUnit> {

    private final CollectionStrategy collectionStrategy = new ClassLoaderCollectionStrategy(
            MavenProjectClassLoaderFactory.getClassLoader());

    private final AstCursorBuffer buffer;

    AstCursor(AstCursorBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean hasNext() {
        if (buffer.getCompilationUnitIterator().hasNext()) {
            // cus还有
            return true;
        } else if (buffer.getSourceRootIterator().hasNext()) {
            // cus没有了，sourceRoots还有 -> 清空cus，tryToParse下一个sourceRoot，重新收集cus
            buffer.getCompilationUnits().clear();
            SourceRoot sourceRoot = buffer.getSourceRootIterator().next();
            buffer.setCurrentSourceRoot(sourceRoot);
            sourceRoot.tryToParseParallelized().forEach(parseResult -> {
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
            Path projectPath = buffer.getProjectPathIterator().next();
            List<SourceRoot> sourceRoots = collectionStrategy.collect(projectPath).getSourceRoots();
            buffer.getSourceRoots().addAll(sourceRoots);
            buffer.setSourceRootIterator(buffer.getSourceRoots().iterator());
            buffer.setCurrentProjectPath(projectPath);
            buffer.setCurrentProjectAstForest(new ProjectAstForest(projectPath, sourceRoots));
            // 递归原因同上
            return hasNext();
        } else {
            // cus、sourceRoots、projectPaths都没有了，递归结束
            return false;
        }
    }

    @Override
    public CompilationUnit next() {
        return buffer.getCompilationUnitIterator().next();
    }

    public void reset() {
        buffer.setProjectPathIterator(buffer.getProjectPaths().iterator());
        buffer.getSourceRoots().clear();
        buffer.setSourceRootIterator(buffer.getSourceRoots().iterator());
        buffer.getSourceRoots().clear();
        buffer.setCompilationUnitIterator(buffer.getCompilationUnits().iterator());
    }

}
