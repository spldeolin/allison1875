package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.service.AstFilterService;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-12-06
 */
@Log4j2
public class Allison1875 {

    public static final String SHORT_VERSION = "1001S";

    private static final String version = "Allison 1875 10.1-SNAPSHOT";

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
            // valid
            List<InvalidDto> invalids = module.validConfigs();
            if (!invalids.isEmpty()) {
                for (InvalidDto invalid : invalids) {
                    log.error("Allison 1875 fail to work cause invalid config, path={}, reason={}, value={}",
                            invalid.getPath(), invalid.getReason(), invalid.getValue());
                }
                System.exit(-9);
            }

            // build AST forest
            AstFilterService astFilterService = injector.getInstance(AstFilterService.class);
            AstForest astForest = new AstForest(primaryClass, false, astFilterService);

            // process main services
            log.info("process main service [{}], module={}", module.declareMainService().getName(), module);
            injector.getInstance(module.declareMainService()).process(astForest);
        }
    }

}