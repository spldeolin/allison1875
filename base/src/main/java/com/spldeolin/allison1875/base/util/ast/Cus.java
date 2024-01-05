package com.spldeolin.allison1875.base.util.ast;

import java.io.File;
import java.nio.file.Path;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.base.exception.CompilationUnitParseException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Log4j2
public class Cus {

    private Cus() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CompilationUnit parseCu(Path javaFilePath) throws CompilationUnitParseException {
        return parseCu(javaFilePath.toFile());
    }

    public static CompilationUnit parseCu(File javaFile) throws CompilationUnitParseException {
        if (!javaFile.exists()) {
            throw new CompilationUnitParseException(String.format("javaFile [%s] not exists", javaFile));
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            log.debug("CompilationUnit@{} <- SourceCode {}", cu.hashCode(),
                    Locations.getStorage(cu).getSourceRoot().relativize(Locations.getAbsolutePath(cu)));
            return cu;
        } catch (Exception e) {
            throw new CompilationUnitParseException(String.format("fail to parse [%s]", javaFile), e);
        }
    }

}