package com.spldeolin.allison1875.base.util.ast;

import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * 将CompilationUnit保存到硬盘
 *
 * @author Deolin 2020-01-26
 */
@Log4j2
public class Saves {

    private static final ThreadLocal<Set<CompilationUnit>> apiSaveBuffer = ThreadLocal.withInitial(Sets::newHashSet);

    private Saves() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 使用这种方式加入的CU对象在SaveAll时，会使用Javaparser的 Code Format
     */
    public static void add(CompilationUnit cu) {
        apiSaveBuffer.get().add(cu);
    }

    @Data
    @AllArgsConstructor
    public static class Replace {

        private String target;

        private String replacement;

    }

    public static void saveAll() {
        for (CompilationUnit cu : apiSaveBuffer.get()) {
            Storage storage = cu.getStorage().orElseThrow(StorageAbsentException::new);
            storage.save();
        }
        apiSaveBuffer.get().clear();
    }

}
