package com.spldeolin.allison1875.base.service;

import java.io.File;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.service.impl.AstFilterServiceImpl;

/**
 * @author Deolin 2024-01-04
 */
@ImplementedBy(AstFilterServiceImpl.class)
public interface AstFilterService {

    boolean accept(File javaFile);

    boolean accept(CompilationUnit cu);

}
