package com.spldeolin.allison1875.base.util.ast;

import java.io.IOException;
import java.nio.file.Path;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Log4j2
public class Cus {

    public static CompilationUnit parseCu(Path absPath) {
        if (!absPath.toFile().exists()) {
            return null;
        }
        CompilationUnit designCu;
        try {
            designCu = StaticJavaParser.parse(absPath);
        } catch (IOException e) {
            log.error("Failed to parse [{}]", absPath, e);
            return null;
        }
        return designCu;
    }

}