package com.spldeolin.allison1875.common.service;

import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.ImportExprServiceImpl;

/**
 * @author Deolin 2024-02-09
 */
@ImplementedBy(ImportExprServiceImpl.class)
public interface ImportExprService {

    CompilationUnit extractQualifiedTypeToImport(CompilationUnit cu);

}