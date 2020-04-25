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

    private final Collection<Path> projectPaths;

    private Iterator<Path> projectPathIterator;

    private Path currentProjectPath;

    private final Collection<SourceRoot> sourceRoots = Lists.newLinkedList();

    private Iterator<SourceRoot> sourceRootIterator = sourceRoots.iterator();

    private SourceRoot currentSourceRoot;

    private final Collection<CompilationUnit> compilationUnits = Lists.newLinkedList();

    private Iterator<CompilationUnit> compilationUnitIterator = compilationUnits.iterator();

    private CompilationUnit currentCompilationUnit;

    private ProjectAstForest currentProjectAstForest;

    AstCursorBuffer(Collection<Path> projectPath) {
        this.projectPaths = projectPath;
        projectPathIterator = projectPaths.iterator();
    }

}
