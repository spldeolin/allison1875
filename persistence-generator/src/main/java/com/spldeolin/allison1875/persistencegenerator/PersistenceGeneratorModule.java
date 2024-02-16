package com.spldeolin.allison1875.persistencegenerator;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import lombok.ToString;

/**
 * @author Deolin 2020-12-08
 */
@ToString
public class PersistenceGeneratorModule extends Allison1875Module {

    private final PersistenceGeneratorConfig persistenceGeneratorConfig;

    public PersistenceGeneratorModule(PersistenceGeneratorConfig persistenceGeneratorConfig) {
        this.persistenceGeneratorConfig = persistenceGeneratorConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return PersistenceGenerator.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return persistenceGeneratorConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
    }

}