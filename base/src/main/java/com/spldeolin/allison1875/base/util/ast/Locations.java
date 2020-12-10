package com.spldeolin.allison1875.base.util.ast;

import java.nio.file.Path;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;

/**
 * 获取代码的位置
 *
 * @author Deolin 2020-02-09
 */
public class Locations {

    private Locations() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * @return e.g.: /Users/deolin/Documents/allison1875/base/src/main/java/com/spldeolin/allison1875/base/util
     *         /Locations.java
     */
    public static Path getAbsolutePath(Node node) {
        return getStorage(node).getPath();
    }

    /**
     * @return e.g.: /Users/deolin/Documents/allison1875/base/src/main/java/com/spldeolin/allison1875/base/util
     *         /Locations.java:23
     */
    public static String getAbsolutePathWithLineNo(Node node) {
        return getAbsolutePath(node) + ":" + getBeginLine(node);
    }

    /**
     * @return e.g.: Locations.java:23
     */
    public static String getFileNameWithLineNo(Node node) {
        return getStorage(node).getFileName() + ":" + getBeginLine(node);
    }

    public static Storage getStorage(Node node) {
        return node.findCompilationUnit().orElseThrow(CuAbsentException::new).getStorage()
                .orElseThrow(StorageAbsentException::new);
    }

    public static int getBeginLine(Node node) {
        return getRange(node).begin.line;
    }

    public static Range getRange(Node node) {
        return node.getRange().orElseThrow(RangeAbsentException::new);
    }

}
