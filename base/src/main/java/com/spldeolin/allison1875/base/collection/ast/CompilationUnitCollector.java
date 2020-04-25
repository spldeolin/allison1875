package com.spldeolin.allison1875.base.collection.ast;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器（基于GlobalCollectionStrategy提供的策略收集）
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
@Accessors(fluent = true)
class CompilationUnitCollector {

    @Setter
    private Collection<SourceRoot> sourceRoots;

    @Getter
    @Setter
    private Collection<CompilationUnit> list;

    @Getter
    private Map<Path, CompilationUnit> map;

    CompilationUnitCollector collectIntoCollection() {
        if (sourceRoots == null) {
            throw new IllegalStateException("sourceRoots cannot be absent.");
        }

        list = Lists.newLinkedList();
        sourceRoots.forEach(this::parseSourceRoot);
        return this;
    }

    CompilationUnitCollector collectIntoMap() {
        if (list == null) {
            throw new IllegalStateException("must call collectIntoCollection() firstly.");
        }

        map = Maps.newHashMapWithExpectedSize(list.size());
        for (CompilationUnit cu : list) {
            cu.getStorage().ifPresent(storage -> map.put(storage.getPath(), cu));
        }
        log.info("(Summary) {} CompilationUnit has collected into Map.", map.size());
        return this;
    }

    private void parseSourceRoot(SourceRoot sourceRoot) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (ParseResult<CompilationUnit> parseResult : sourceRoot.tryToParseParallelized()) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(list::add);
                count++;
            } else {
                log.warn("Parse with problems, ignore and continue. [{}]", parseResult.getProblems());
            }
        }

        if (count > 0) {
            log.info("(Detail) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", count,
                    sourceRoot.getRoot(), System.currentTimeMillis() - start);
        }
    }

}
