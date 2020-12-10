package com.spldeolin.allison1875.base;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.GuiceUtils;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-12-06
 */
@Log4j2
public class Allison1875 {

    public static void allison1875(Class<?> primaryClass, Module... guiceModules) {
        Greeting.version();
        Injector injector = GuiceUtils.createInjector(guiceModules);

        AstForest astForest = new AstForest(primaryClass);
        for (Module guiceModule : guiceModules) {
            if (guiceModule instanceof Allison1875Module) {
                ((Allison1875Module) guiceModule).getMainProcessor(injector).process(astForest);
            }
        }
    }

}