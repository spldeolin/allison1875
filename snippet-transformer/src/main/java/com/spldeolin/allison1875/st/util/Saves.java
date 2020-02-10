package com.spldeolin.allison1875.st.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;

/**
 * 将CompilationUnit保存到硬盘
 *
 * @author Deolin 2020-01-26
 */
public class Saves {

    /**
     * 以代码格式化的形式进行保存
     */
    public static void prettySave(CompilationUnit cu) {
        cu.getStorage().orElseThrow(StorageAbsentException::new).save();
    }

    /**
     * 以保持代码源格式的形式进行保存
     */
    public static void originalSave(CompilationUnit cu) {
        cu.getStorage().orElseThrow(StorageAbsentException::new).save(LexicalPreservingPrinter::print);
    }

}
