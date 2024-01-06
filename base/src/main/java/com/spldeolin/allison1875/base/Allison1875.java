package com.spldeolin.allison1875.base;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.service.AstFilterService;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-12-06
 */
@Log4j2
public class Allison1875 {

    public static final String SHORT_VERSION = "1000S";

    private static final String version = "Allison 1875 10.0-SNAPSHOT";

    static {
        String banner;
        try {
            banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        System.out.println(banner);
        System.out.println(version);
        System.out.println("More information at: https://github.com/spldeolin/allison1875");
        System.out.println();
    }

    public static void allison1875(Class<?> primaryClass, Allison1875Module... allison1875Modules) {
        // check args
        Preconditions.checkArgument(primaryClass != null, "required 'primaryClass' Parameter cannot be null");
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(allison1875Modules),
                "requried 'allison1875Modules' Parameter cannot be empty");

        // create ioc container
        Injector injector = Guice.createInjector(allison1875Modules);

        for (Allison1875Module module : allison1875Modules) {

            // build AST forest
            AstFilterService astFilterService = injector.getInstance(AstFilterService.class);
            AstForest astForest = new AstForest(primaryClass, false, astFilterService);

            // process main services
            log.info("process main service [{}], module={}", module.declareMainService().getName(), module);
            injector.getInstance(module.declareMainService()).process(astForest);
        }
    }

}