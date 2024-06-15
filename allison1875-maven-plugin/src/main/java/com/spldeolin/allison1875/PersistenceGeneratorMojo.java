package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "persistence-generator", requiresDependencyResolution = ResolutionScope.RUNTIME)
@Slf4j
public class PersistenceGeneratorMojo extends Allison1875Mojo {

    @Parameter(defaultValue = "com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule")
    private String persistenceGeneratorModuleQualifier;

    @Parameter
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        persistenceGeneratorConfig = MoreObjects.firstNonNull(persistenceGeneratorConfig,
                new PersistenceGeneratorConfig());
        persistenceGeneratorConfig.setTables(
                MoreObjects.firstNonNull(persistenceGeneratorConfig.getTables(), Lists.newArrayList()));
        persistenceGeneratorConfig.setEnableGenerateDesign(
                MoreObjects.firstNonNull(persistenceGeneratorConfig.getEnableGenerateDesign(), true));
        persistenceGeneratorConfig.setIsEntityEndWithEntity(
                MoreObjects.firstNonNull(persistenceGeneratorConfig.getIsEntityEndWithEntity(), true));
        persistenceGeneratorConfig.setEntityExistenceResolution(
                MoreObjects.firstNonNull(persistenceGeneratorConfig.getEntityExistenceResolution(),
                        FileExistenceResolutionEnum.OVERWRITE));
        log.info("persistenceGeneratorConfig={}", JsonUtils.toJsonPrettily(persistenceGeneratorConfig));

        return (Allison1875Module) classLoader.loadClass(persistenceGeneratorModuleQualifier)
                .getConstructor(CommonConfig.class, PersistenceGeneratorConfig.class)
                .newInstance(commonConfig, persistenceGeneratorConfig);
    }

}
