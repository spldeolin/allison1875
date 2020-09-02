package com.spldeolin.allison1875.base.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 可遍历的抽象语法树森林
 *
 * @author Deolin 2020-04-24
 */
@Log4j2
public class AstForest implements Iterable<CompilationUnit> {

    private final Class<?> anyClassFromHost;

    private final ImmutableList<String> dependencyProjectPaths;

    @Getter
    private final Path host;

    @Getter
    private final Path hostSourceRoot;

    private final ImmutableList<Path> hostAndDependencySourceRoots;

    @Getter
    private final Path commonPathPart;

    private AstCursor cursor;

    public AstForest(Class<?> anyClassFromHost) {
        this.anyClassFromHost = anyClassFromHost;
        this.dependencyProjectPaths = null;

        this.host = detectHost(anyClassFromHost);
        this.hostSourceRoot = detectHostSourceRoot(host);
        this.hostAndDependencySourceRoots = ImmutableList.of(hostSourceRoot);
        this.commonPathPart = calcCommonPath(hostAndDependencySourceRoots);
        this.cursor = new AstCursor(commonPathPart, hostAndDependencySourceRoots);
    }

    public AstForest(Class<?> anyClassFromHost, Collection<String> dependencyProjectPaths) {
        this.anyClassFromHost = anyClassFromHost;
        this.dependencyProjectPaths = ImmutableList.copyOf(dependencyProjectPaths);

        this.host = detectHost(anyClassFromHost);
        this.hostSourceRoot = detectHostSourceRoot(host);
        List<Path> sourceRoots = collectDependencySourceRoots(dependencyProjectPaths);
        sourceRoots.add(0, hostSourceRoot);
        this.hostAndDependencySourceRoots = ImmutableList.copyOf(sourceRoots);
        this.commonPathPart = calcCommonPath(hostAndDependencySourceRoots);
        this.cursor = new AstCursor(commonPathPart, hostAndDependencySourceRoots);
    }

    private Path detectHost(Class<?> anyClassFromHost) {
        return CodeGenerationUtils.mavenModuleRoot(anyClassFromHost);
    }

    private Path detectHostSourceRoot(Path host) {
        Path hostSourceRootPath = host.resolve(BaseConfig.getInstance().getJavaDirectoryLayout());
        return hostSourceRootPath;
    }

    private List<Path> collectDependencySourceRoots(Collection<String> dependencyProjectPaths) {
        List<Path> result = Lists.newArrayList();
        Collection<SourceRoot> sourceRoots = new SourceRootCollector().collect(dependencyProjectPaths);
        for (SourceRoot sourceRoot : sourceRoots) {
            log.info("dependencySourceRootPath={}", sourceRoot.getRoot());
            result.add(sourceRoot.getRoot());
        }
        return result;
    }

    private Path calcCommonPath(Collection<Path> sourceRootPaths) {
        List<Path> paths = Lists.newArrayList(sourceRootPaths);
        String common = paths.get(0).toString();
        for (Path path : paths) {
            common = Strings.commonPrefix(common, path.toString());
        }
        return Paths.get(common);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cursor;
    }

    public AstForest reset() {
        log.info("Astforest reset.");
        this.cursor = new AstCursor(commonPathPart, hostAndDependencySourceRoots);
        return this;
    }

    @Override
    public String toString() {
        return "AstForest{" + "anyClassFromHost=" + anyClassFromHost + ", dependencyProjectPaths="
                + dependencyProjectPaths + ", hostSourceRoot=" + hostSourceRoot + ", hostAndDependencySourceRoots="
                + hostAndDependencySourceRoots + ", commonPathPart=" + commonPathPart + '}';
    }

}
