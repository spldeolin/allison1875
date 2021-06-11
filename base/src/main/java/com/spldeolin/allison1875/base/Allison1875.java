package com.spldeolin.allison1875.base;

import java.util.Locale;
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

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void allison1875(Class<?> primaryClass, Module... guiceModules) {
        // Version
        Version.greeting();

        // 启动IOC
        Injector injector = GuiceUtils.createInjector(guiceModules);

        // 运行主流程
        launch(primaryClass, injector, guiceModules);
    }

    private static void launch(Class<?> primaryClass, Injector injector, Module[] guiceModules) {
        for (Module guiceModule : guiceModules) {
            if (guiceModule instanceof Allison1875Module) {
                Allison1875Module allison1875Module = (Allison1875Module) guiceModule;
                allison1875Module.validateConfig(injector);
                AstForest astForest = new AstForest(primaryClass, false);
                allison1875Module.launchMainProcessor(astForest, injector);
            }
        }
    }

}