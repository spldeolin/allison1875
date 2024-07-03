package com.spldeolin.allison1875.extension.satisficing;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.extension.satisficing.persistencegenerator.CommentServiceImpl2;
import com.spldeolin.allison1875.extension.satisficing.persistencegenerator.JdbcTypeServiceImpl2;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.service.CommentService;
import com.spldeolin.allison1875.persistencegenerator.service.JdbcTypeService;

/**
 * @author Deolin 2024-06-15
 */
public class PersistenceGeneratorModule extends
        com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule {

    private final CommonConfig commonConfig;

    public PersistenceGeneratorModule(CommonConfig commonConfig,
            PersistenceGeneratorConfig persistenceGeneratorConfig) {
        super(commonConfig, persistenceGeneratorConfig);
        this.commonConfig = commonConfig;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(JdbcTypeService.class).toInstance(new JdbcTypeServiceImpl2(commonConfig.getBasePackage() + ".enums"));
        bind(CommentService.class).toInstance(new CommentServiceImpl2());
    }

}
