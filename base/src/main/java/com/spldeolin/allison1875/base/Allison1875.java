package com.spldeolin.allison1875.base;

import java.util.Locale;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.ast.AstForest;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-12-06
 */
@Log4j2
public class Allison1875 {

    public static void allison1875(Class<?> primaryClass, Allison1875Module... allison1875Modules) {
        Version.greeting();
        Locale.setDefault(Locale.ENGLISH);

        // argument check
        Preconditions.checkArgument(primaryClass != null, "required 'primaryClass' Parameter cannot be null");
        Preconditions.checkArgument(allison1875Modules.length > 0,
                "requried 'allison1875Modules' Parameter cannot be empty");

        // report
        for (Allison1875Module allison1875Module : allison1875Modules) {
            log.info("module [{}]", allison1875Module);
        }

        // ioc
        Injector injector = Guice.createInjector(allison1875Modules);

        // launch main proecssors
        AstForest astForest = new AstForest(primaryClass, false);
        for (int i = 0; i < allison1875Modules.length; i++) {
            Allison1875Module allison1875Module = allison1875Modules[i];
            if (i > 0) {
                astForest.reset();
            }
            injector.getInstance(allison1875Module.provideMainProcessorType()).process(astForest);
        }
    }

}