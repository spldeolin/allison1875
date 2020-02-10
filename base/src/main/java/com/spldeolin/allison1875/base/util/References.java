package com.spldeolin.allison1875.base.util;

import java.nio.file.Path;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.Config;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;

/**
 * @author Deolin 2020-02-09
 */
public class References {

    public static Path getRelativePath(Node node) {
        Storage storage = getStorageOrElseThrow(node);
        return Config.getProjectPath().relativize(storage.getPath());
    }

    public static Storage getStorageOrElseThrow(Node node) {
        return node.findCompilationUnit().orElseThrow(CuAbsentException::new).getStorage()
                .orElseThrow(StorageAbsentException::new);
    }

    public static Range getRangeOrElseThrow(Node node) {
        return node.getRange().orElseThrow(RangeAbsentException::new);
    }

    public static int getBeginLine(Node node) {
        return getRangeOrElseThrow(node).begin.line;
    }

}
