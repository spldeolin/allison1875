package com.spldeolin.allison1875.base.ast.collection;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.Config;
import com.spldeolin.allison1875.base.ast.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.allison1875.base.ast.classloader.WarOrFatJarClassLoader;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
class CompilationUnitCollector {

    Collection<CompilationUnit> collect(Path path) {
        long start = System.currentTimeMillis();
        Collection<CompilationUnit> result = Lists.newLinkedList();
        CollectionStrategy strategy = new ClassLoaderCollectionStrategy(WarOrFatJarClassLoader.classLoader);
        collectSoruceRoots(path, strategy).forEach(sourceRoot -> parseSourceRoot(sourceRoot, result));
        log.info("(Summary) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", result.size(),
                path.toAbsolutePath(), System.currentTimeMillis() - start);
        return result;
    }


    private Collection<SourceRoot> collectSoruceRoots(Path path, CollectionStrategy collectionStrategy) {
        ProjectRoot projectRoot = collectionStrategy.collect(path);
        projectRoot.addSourceRoot(path);
        return projectRoot.getSourceRoots();
    }

    private void parseSourceRoot(SourceRoot sourceRoot, Collection<CompilationUnit> all) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (ParseResult<CompilationUnit> parseResult : sourceRoot.tryToParseParallelized()) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(all::add);
                count++;
            } else {
                log.warn("Parse with problem, ignore and continue. [{}]", parseResult.getProblems());
            }
        }

        if (count > 0) {
            log.info("(Detail) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", count,
                    "../" + Config.getProjectPath().relativize(sourceRoot.getRoot()),
                    System.currentTimeMillis() - start);
        }
    }

}
