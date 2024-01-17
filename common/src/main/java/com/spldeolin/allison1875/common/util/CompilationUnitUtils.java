package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.nio.file.Path;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.common.exception.CompilationUnitParseException;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.exception.StorageAbsentException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Log4j2
public class CompilationUnitUtils {

    private CompilationUnitUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static CompilationUnit parseJava(File javaFile) throws CompilationUnitParseException {
        if (!javaFile.exists()) {
            throw new CompilationUnitParseException(String.format("javaFile [%s] not exists", javaFile));
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            log.debug("SourceCode parsed {}", getCuAbsolutePath(cu));
            return cu;
        } catch (Exception e) {
            throw new CompilationUnitParseException(String.format("fail to parse [%s]", javaFile), e);
        }
    }

    /**
     * 获取参数node所在CU的绝对路径
     *
     * @return e.g.: /Users/deolin/Documents/allison1875/common/src/main/java/com/spldeolin/allison1875/common/util
     *         /Locations.java
     */
    public static Path getCuAbsolutePath(Node node) throws CuAbsentException, StorageAbsentException {
        return node.findCompilationUnit().orElseThrow(CuAbsentException::new).getStorage()
                .orElseThrow(StorageAbsentException::new).getPath();
    }

}