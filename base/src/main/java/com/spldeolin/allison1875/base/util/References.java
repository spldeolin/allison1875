package com.spldeolin.allison1875.base.util;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;

/**
 * @author Deolin 2020-02-09
 */
public class References {

    public static Storage getStorageOrElseThrow(Node node) {
        return node.findCompilationUnit().orElseThrow(CuAbsentException::new).getStorage()
                .orElseThrow(StorageAbsentException::new);
    }

    public static Range getRangeOrElseThrow(Node node) {
        return node.getRange().orElseThrow(RangeAbsentException::new);
    }

}
