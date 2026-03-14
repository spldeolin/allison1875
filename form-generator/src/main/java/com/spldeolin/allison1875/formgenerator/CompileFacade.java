package com.spldeolin.allison1875.formgenerator;

import com.spldeolin.allison1875.common.ast.AstForest;

/**
 * @author Deolin 2026-02-27
 */
public interface CompileFacade {

    void compile(AstForest astForest, String javaVersion);

}
