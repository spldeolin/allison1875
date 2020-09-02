package com.spldeolin.allison1875.base;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-08-29
 */
public class Allison1875 {

    public static void allison1875(Class<?> primaryClass, Allison1875MainProcessor... processors) {
        AstForest astForest = new AstForest(primaryClass);
        for (Allison1875MainProcessor processor : processors) {
            processor.process(astForest);
        }
    }

}