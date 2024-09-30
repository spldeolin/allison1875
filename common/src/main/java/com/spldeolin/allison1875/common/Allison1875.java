package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.interceptor.ValidInterceptor;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Allison1875
 *
 * @author Deolin 2020-12-06
 */
@Slf4j
public class Allison1875 {

    public static final String SHORT_VERSION = "1100R";

    public static void hello() {
        String banner;
        try {
            banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info(banner);
    }

    public static void letsGo(Allison1875Module allison1875Module, AstForest astForest) {
        // valid config
        List<InvalidDto> invalids = allison1875Module.validConfigs();
        if (CollectionUtils.isNotEmpty(invalids)) {
            throw new Allison1875Exception(
                    "Allison 1875 fail to work cause invalid config\ninvalids=" + JsonUtils.toJsonPrettily(invalids));
        }

        // register interceptor
        List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidInterceptor().toGuiceModule());

        // create ioc container
        Injector injector = Guice.createInjector(guiceModules);

        // process main service
        injector.getInstance(allison1875Module.declareMainService()).process(astForest);
    }

}