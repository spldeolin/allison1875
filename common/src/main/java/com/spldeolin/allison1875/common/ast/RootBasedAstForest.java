package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.service.AstFilterService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.Getter;

/**
 * @author Deolin 2024-01-19
 */
public class RootBasedAstForest implements Iterable<CompilationUnit> {

    @Getter
    private final File astForestRoot;

    private final AstFilterService astFilterService;

    public RootBasedAstForest(File astForestRoot) {
        this(astForestRoot, null);
    }

    public RootBasedAstForest(File astForestRoot, AstFilterService astFilterService) {
        Preconditions.checkNotNull(astForestRoot, "required Argument 'astForestRoot' must not be null");
        this.astForestRoot = astForestRoot;
        this.astFilterService = astFilterService;
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        // java files
        Iterator<File> javaFilesItr = FileUtils.iterateFiles(astForestRoot, new String[]{"java"}, true);
        // filtered java files
        if (astFilterService != null) {
            javaFilesItr = Iterators.filter(javaFilesItr, astFilterService::accept);
        }
        // cus
        Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
        // filtered cus
        if (astFilterService != null) {
            cusItr = Iterators.filter(cusItr, astFilterService::accept);
        }
        return cusItr;
    }

}