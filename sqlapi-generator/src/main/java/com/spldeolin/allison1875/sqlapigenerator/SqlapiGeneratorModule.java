package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.service.impl.JavabeanGeneratorServiceQualifiedTypeImpl;

/**
 * @author Deolin 2024-01-20
 */
public class SqlapiGeneratorModule extends Allison1875Module {

    private final SqlapiGeneratorConfig sqlapiGeneratorConfig;

    public SqlapiGeneratorModule(SqlapiGeneratorConfig sqlapiGeneratorConfig) {
        this.sqlapiGeneratorConfig = sqlapiGeneratorConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return SqlapiGenerator.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return sqlapiGeneratorConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(SqlapiGeneratorConfig.class).toInstance(sqlapiGeneratorConfig);
        bind(JavabeanGeneratorService.class).toInstance(new JavabeanGeneratorServiceQualifiedTypeImpl());
    }

}