package com.spldeolin.allison1875.base.ast;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.spldeolin.allison1875.base.util.ast.Locations;

/**
 * @author Deolin 2023-05-26
 */
public class FileFlush {

    private File src;

    private String newContent;

    private FileFlush() {
    }

    public static FileFlush build(CompilationUnit cu) {
        FileFlush result = new FileFlush();
        result.src = Locations.getAbsolutePath(cu).toFile();
        result.newContent = cu.toString();
        return result;
    }

    public static FileFlush buildLexicalPreserving(CompilationUnit cu) {
        FileFlush result = new FileFlush();
        result.src = Locations.getAbsolutePath(cu).toFile();
        result.newContent = LexicalPreservingPrinter.print(cu);
        return result;
    }

    public static FileFlush build(File src, String newContent) {
        FileFlush result = new FileFlush();
        result.src = src;
        result.newContent = newContent;
        return result;
    }

    public void flush() {
        try {
            FileUtils.writeStringToFile(src, newContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}