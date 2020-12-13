package com.spldeolin.allison1875.base.ancestor;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-10
 */
@Log4j2
public abstract class Allison1875Module extends AbstractModule {

    protected abstract Class<? extends Allison1875MainProcessor> provideMainProcessorType();

    protected abstract Set<Class<?>> provideConfigTypes();

    public boolean validateConfig(Injector injector) {
        for (Class<?> configType : getProvidedConfigWithBaseConfigType()) {
            Object component = injector.getInstance(configType);
            log.info("detect config properties {}", component);
            Set<ConstraintViolation<Object>> violations = ValidateUtils.validate(component);
            if (violations.size() > 0) {
                log.error("配置项校验未通过，请检查后重新运行");
            }
            for (ConstraintViolation<?> violation : violations) {
                log.error(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            if (violations.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void launchMainProcessor(AstForest astForest, Injector injector) {
        Class<? extends Allison1875MainProcessor> mainProcessorType = provideMainProcessorType();
        if (mainProcessorType == null) {
            throw new IllegalArgumentException("Allison1875Module#provideMainProcessorType实现的返回值不能为null");
        }
        Allison1875MainProcessor mainProcessor = injector.getInstance(mainProcessorType);
        mainProcessor.process(astForest);
    }

    public Collection<Object> getConfigs(Injector injector) {
        Collection<Object> result = Lists.newArrayList();
        for (Class<?> configType : getProvidedConfigWithBaseConfigType()) {
            result.add(injector.getInstance(configType));
        }
        return result;
    }

    private Set<Class<?>> getProvidedConfigWithBaseConfigType() {
        Set<Class<?>> configTypes = provideConfigTypes();
        configTypes.add(BaseConfig.class);
        return configTypes;
    }

}