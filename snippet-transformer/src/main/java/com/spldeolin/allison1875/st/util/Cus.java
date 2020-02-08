package com.spldeolin.allison1875.st.util;

import java.nio.file.Path;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;

/**
 * @author Deolin 2020-01-26
 */
public class Cus {

    public static Path getSourceRoot(CompilationUnit cu) {
        return cu.getStorage().orElseThrow(StorageAbsentException::new).getSourceRoot();
    }

    public static void prettySave(CompilationUnit cu) {
        cu.getStorage().orElseThrow(StorageAbsentException::new).save();
    }

    public static void originalSave(CompilationUnit cu) {
        cu.getStorage().orElseThrow(StorageAbsentException::new).save(LexicalPreservingPrinter::print);
    }

}
