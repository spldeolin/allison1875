package com.spldeolin.allison1875.common.service.impl;

import java.io.File;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AstFilterService;

/**
 * @author Deolin 2024-01-04
 */
@Singleton
public class AcceptAllAstFilterService implements AstFilterService {

    @Override
    public boolean accept(File javaFile) {
        // default implement will accept all
        return true;
    }

    @Override
    public boolean accept(CompilationUnit cu) {
        // default implement will accept all
        return true;
    }

}
