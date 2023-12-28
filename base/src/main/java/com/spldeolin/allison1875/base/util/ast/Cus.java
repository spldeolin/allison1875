package com.spldeolin.allison1875.base.util.ast;

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

    public static CompilationUnit parseCu(Path path) throws CompilationUnitParseException {
        if (!path.toFile().exists()) {
            throw new CompilationUnitParseException(String.format("path [%s] not exists", path));
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(path);
            log.debug("CompilationUnit@{} <- SourceCode {}", cu.hashCode(),
                    Locations.getStorage(cu).getSourceRoot().relativize(Locations.getAbsolutePath(cu)));
            return cu;
        } catch (Exception e) {
            throw new CompilationUnitParseException(String.format("fail to parse [%s]", path), e);
        }
    }

}