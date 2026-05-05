package com.spldeolin.allison1875.common.ast;

import java.nio.file.Path;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;

/**
 * @author Deolin 2024-06-10
 */
public interface AstForest extends Iterable<CompilationUnit> {

    AstForest cloneWithResetting();

    ClassLoader getClassLoader();

    /**
     * 获取所有的SourceRoot路径
     */
    Set<Path> getSourceRoots();

    /**
     * 获取主SourceRoot路径（当前执行module的SourceRoot）
     */
    Path getPrimarySourceRoot();

}
