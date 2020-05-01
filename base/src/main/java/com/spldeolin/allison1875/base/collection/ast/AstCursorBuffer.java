package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import lombok.Data;

@Data
public class AstCursorBuffer {

    private final Collection<Path> sourceRoots;

    private Iterator<Path> sourceRootIterator;

    private SourceRoot currentSourceRoot;

    private final Collection<CompilationUnit> compilationUnits = Lists.newLinkedList();

    private Iterator<CompilationUnit> compilationUnitIterator = compilationUnits.iterator();

    private CompilationUnit currentCompilationUnit;

    private ProjectAstForest currentProjectAstForest;

    AstCursorBuffer(Collection<Path> sourceRoots) {
        this.sourceRoots = sourceRoots;
        sourceRootIterator = sourceRoots.iterator();
    }

}
