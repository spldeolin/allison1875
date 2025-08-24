package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Optional;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-12-06
 */
@Slf4j
public class CompilationUnitUtils {

    private CompilationUnitUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CompilationUnit parseJava(File javaFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            log.debug("SourceCode parsed {}", getCuAbsolutePath(cu));
            return cu;
        } catch (FileNotFoundException e) {
            throw new Allison1875Exception(String.format("javaFile '%s' not exists", javaFile));
        } catch (ParseProblemException e) {
            throw new Allison1875Exception(String.format("fail to parse [%s]", javaFile), e);
        }
    }

    /**
     * 获取参数node所在CU的绝对路径
     *
     * @return e.g.: /Users/deolin/Documents/allison1875/common/src/main/java/com/spldeolin/allison1875/common/util
     *         /Locations.java
     */
    public static Path getCuAbsolutePath(CompilationUnit cu) {
        return cu.getStorage().orElseThrow(() -> new Allison1875Exception("Cu [" + cu + "has not set Storage yet"))
                .getPath();
    }

    public static Optional<CompilationUnit> tryFindCu(Path sourceRoot, String primaryTypeQualifier) {
        try {
            Path absPath = sourceRoot.resolve(toRelativePath(primaryTypeQualifier));
            if (!absPath.toFile().exists()) {
                log.debug("cu not exists, qualifier={}", primaryTypeQualifier);
                return Optional.empty();
            }

            return Optional.of(CompilationUnitUtils.parseJava(absPath.toFile()));
        } catch (Exception e) {
            log.debug("cannot find cu, qualifier={}", primaryTypeQualifier, e);
            return Optional.empty();
        }
    }

    private static String toRelativePath(String qualifier) {
        return qualifier.replace('.', File.separatorChar) + ".java";
    }

    public static CompilationUnit newBaseCurrentAstForest() {
        CompilationUnit cu = new CompilationUnit();
        cu.setData(Node.SYMBOL_RESOLVER_KEY,
                new JavaSymbolSolver(new ClassLoaderTypeSolver(AstForestContext.get().getClassLoader())));
        return cu;
    }

}