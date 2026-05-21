package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
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
     * 从InputStream中解析Java源码为CompilationUnit
     *
     * <p>适用于从jar包条目等非文件来源解析Java源码的场景，解析后的CU不包含Storage信息。
     * 内部先将InputStream读为String再解析，避免StaticJavaParser直接读取InputStream时的潜在兼容性问题。
     *
     * @param inputStream Java源码输入流
     * @param sourceName 来源描述（用于日志和异常信息），例如jar条目路径
     * @return 解析后的CompilationUnit
     */
    public static CompilationUnit parseJava(InputStream inputStream, String sourceName) {
        String sourceCode;
        try {
            sourceCode = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Allison1875Exception(String.format("fail to read [%s]", sourceName), e);
        }
        if (sourceCode.isBlank()) {
            log.warn("empty source content from: {}", sourceName);
            return new CompilationUnit();
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            log.debug("SourceCode parsed from stream: {}", sourceName);
            return cu;
        } catch (ParseProblemException e) {
            throw new Allison1875Exception(String.format("fail to parse [%s]", sourceName), e);
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

    public static void writeJava(CompilationUnit cu) {
        writeJava(cu, false);
    }

    public static void writeJava(CompilationUnit cu, boolean lexicalPreserving) {
        String content;
        if (lexicalPreserving) {
            content = LexicalPreservingPrinter.print(cu);
        } else {
            content = cu.toString();
        }
        try {
            Path path = getCuAbsolutePath(cu);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}