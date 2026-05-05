package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.guice.ValidationModule;
import lombok.extern.slf4j.Slf4j;

/**
 * Allison1875
 *
 * @author Deolin 2020-12-06
 */
@Slf4j
public class Allison1875 {

    public static void hello() {
        try (Reader reader = Resources.asCharSource(Resources.getResource("allison1875-git.properties"),
                StandardCharsets.UTF_8).openStream()) {

            // read allison1875-banner.txt
            String banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);

            // read allison1875-git.properties
            Properties properties = new Properties();
            properties.load(reader);
            banner = banner.replace("${commitId}", properties.getProperty("git.commit.id.abbrev"));

            // replace placeholders
            String version = properties.getProperty("git.build.version");
            banner = banner.replace("${buildVersion}", version);

            // print banner
            log.info(banner);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void letsGo(Allison1875Module allison1875Module, AstForest astForest, DomainConfig domainConfig) {
        // append built-in guice modules
        List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidationModule());

        // create guice container
        Injector injector;
        try {
            injector = Guice.createInjector(guiceModules);
        } catch (CreationException e) {
            if (e.getCause() instanceof Allison1875Exception) {
                throw (Allison1875Exception) e.getCause();
            }
            throw e;
        }

        // process main service
        AstForestContext.set(astForest);
        DomainContext.set(domainConfig);
        try {
            injector.getInstance(allison1875Module.declareMainService()).process();
        } catch (Throwable e) {
            log.error("main process failed", e);
            throw new Allison1875Exception(e);
        }
    }

}