package com.spldeolin.allison1875.base;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProc;
import com.spldeolin.allison1875.base.collection.ast.AstForest;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-08-29
 */
public class Allison1875 {

    public static void boot(AstForest astForest, Allison1875MainProc... mainProcs) {
        for (Allison1875MainProc mainProc : mainProcs) {
            mainProc.process(astForest);
        }
    }

}