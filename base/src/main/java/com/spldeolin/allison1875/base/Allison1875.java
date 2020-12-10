package com.spldeolin.allison1875.base;

import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.GuiceUtils;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-12-06
 */
@Log4j2
public class Allison1875 {

    public static void allison1875(Class<?> primaryClass, Module... guiceModules) {
        Greeting.version();
        Injector injector = GuiceUtils.createInjector(guiceModules);

        AstForest astForest = new AstForest(primaryClass);
        for (Module guiceModule : guiceModules) {
            guiceModule.getMainProcessor(injector).process(astForest);
        }
    }

    public static abstract class Module extends AbstractModule {

        public <T> T ensureValid(T config) {
            Set<ConstraintViolation<Object>> violations = ValidateUtils.validate(config);
            if (violations.size() > 0) {
                log.warn("配置项校验未通过，请检查后重新运行");
                for (ConstraintViolation<Object> violation : violations) {
                    log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                            + violation.getMessage());
                }
                System.exit(-9);
            }
            return config;
        }

        public abstract Allison1875MainProcessor getMainProcessor(Injector injector);

    }

}