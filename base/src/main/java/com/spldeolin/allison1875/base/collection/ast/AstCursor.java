package com.spldeolin.allison1875.base.collection.ast;

import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

/**
 * 抽象语法树迭代子
 *
 * @author Deolin 2020-04-23
 */
class AstCursor implements Iterator<CompilationUnit> {

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
            Collection<CompilationUnit> cus = new CompilationUnitCollector().sourceRoot(sourceRoot)
                    .collectIntoCollection().list();
            buffer.getCompilationUnits().addAll(cus);
            buffer.setCompilationUnitIterator(buffer.getCompilationUnits().iterator());
            // 递归的目的是这个sourceRoot可能没源码
            return hasNext();
        } else {
            // cus、sourceRoots没有了，递归结束
            return false;
        }
    }

    @Override
    public CompilationUnit next() {
        return buffer.getCompilationUnitIterator().next();
    }

    public void reset() {
        buffer.getSourceRoots().clear();
        buffer.setSourceRootIterator(buffer.getSourceRoots().iterator());
        buffer.getSourceRoots().clear();
        buffer.setCompilationUnitIterator(buffer.getCompilationUnits().iterator());
    }

}
