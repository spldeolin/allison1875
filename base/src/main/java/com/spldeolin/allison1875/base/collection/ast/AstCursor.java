package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import lombok.extern.log4j.Log4j2;

/**
 * 抽象语法树迭代子
 *
 * @author Deolin 2020-04-23
 */
@Log4j2
class AstCursor implements Iterator<CompilationUnit> {

    private final Path commonPathPart;

    private final Iterator<SourceRoot> sourceRootItr;

    private Iterator<CompilationUnit> cuItr = Collections.emptyIterator();

    AstCursor(Path commonPathPart, Collection<Path> sourceRootPaths) {
        this.commonPathPart = commonPathPart;
        this.sourceRootItr = sourceRootPaths.stream().map(SourceRoot::new).collect(Collectors.toList()).iterator();
    }

    @Override
    public boolean hasNext() {
        if (cuItr.hasNext()) {
            return true;
        } else if (sourceRootItr.hasNext()) {
            // cus没有了，sourceRoots还有 -> 清空cus，tryToParse下一个sourceRoot，重新收集cus

            // remove current one help gc
            try {
                sourceRootItr.remove();
            } catch (Exception ignored) {
            }

            SourceRoot sourceRoot = sourceRootItr.next();
            Collection<CompilationUnit> cus = new CompilationUnitCollector(commonPathPart).collect(sourceRoot);
            cuItr = cus.iterator();
            // 递归的目的是这个sourceRoot可能没源码
            return hasNext();
        } else {
            // cus、sourceRoots没有了，递归结束
            return false;
        }
    }

    @Override
    public CompilationUnit next() {
        return cuItr.next();
    }

}
