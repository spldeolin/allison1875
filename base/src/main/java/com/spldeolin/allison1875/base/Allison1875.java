package com.spldeolin.allison1875.base;

import com.spldeolin.allison1875.base.ancestor.Allison1875ModuleMainProc;
import com.spldeolin.allison1875.base.collection.ast.AstForest;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-08-29
 */
public class Allison1875 {

    public static void boot(AstForest astForest, Allison1875ModuleMainProc... mainProcs) {
        for (Allison1875ModuleMainProc mainProc : mainProcs) {
            mainProc.process(astForest);
        }
    }

}