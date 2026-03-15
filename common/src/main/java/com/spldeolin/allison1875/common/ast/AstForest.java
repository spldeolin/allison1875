package com.spldeolin.allison1875.common.ast;

import java.nio.file.Path;
import com.github.javaparser.ast.CompilationUnit;

/**
 * @author Deolin 2024-06-10
 */
public interface AstForest extends Iterable<CompilationUnit> {

    AstForest cloneWithResetting();

    ClassLoader getClassLoader();

    Path getSourceRoot();

}
