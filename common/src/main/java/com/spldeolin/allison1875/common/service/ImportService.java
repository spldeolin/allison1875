package com.spldeolin.allison1875.common.service;

import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.ImportServiceImpl;

/**
 * @author Deolin 2024-02-09
 */
@ImplementedBy(ImportServiceImpl.class)
public interface ImportService {

    CompilationUnit extractQualifiedTypeToImport(CompilationUnit cu);

}