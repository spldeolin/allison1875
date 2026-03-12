package com.spldeolin.allison1875.mojo;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "star-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class StarTransformerMojo extends Allison1875Mojo {

    @Override
    public Allison1875Module newAllison1875Module(MojoConfig config, ClassLoader classLoader) throws Exception {
        log.info("starTransformerModule={}", config.getStarTransformerModule());
        return (Allison1875Module) classLoader.loadClass(config.getStarTransformerModule()).getConstructor(Config.class)
                .newInstance(config);
    }

}
