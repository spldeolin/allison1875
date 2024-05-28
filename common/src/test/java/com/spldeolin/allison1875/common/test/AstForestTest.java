package com.spldeolin.allison1875.common.test;

import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.service.impl.AcceptAllAstFilterService;
import com.spldeolin.allison1875.common.service.impl.MavenAstForestResidenceService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-17
 */
@Slf4j
public class AstForestTest {

    public static void main(String[] args) {
        AstForest astForest = new AstForest(AstForestTest.class, new MavenAstForestResidenceService(),
                new AcceptAllAstFilterService());
        for (CompilationUnit compilationUnit : astForest) {
            log.info(CompilationUnitUtils.getCuAbsolutePath(compilationUnit).toString());
        }
    }

}