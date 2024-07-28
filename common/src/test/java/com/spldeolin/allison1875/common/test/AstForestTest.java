package com.spldeolin.allison1875.common.test;

import java.io.File;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-17
 */
@Slf4j
public class AstForestTest {

    public static void main(String[] args) {
        AstForest astForest = new MavenProjectBuiltAstForest(AstForestTest.class.getClassLoader(), new File("common/"));
        log.info("resources={}", astForest.resolve("src/main/resources", false).get().getAbsolutePath());
        for (CompilationUnit compilationUnit : astForest) {
            log.info(CompilationUnitUtils.getCuAbsolutePath(compilationUnit).toString());
        }
    }

}