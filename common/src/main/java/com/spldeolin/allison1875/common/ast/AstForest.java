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
     * 获取SourceRoot路径
     */
    Path getSourceRoot();

}
