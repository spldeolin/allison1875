package com.spldeolin.allison1875.base;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.RedissonFactory;
import com.spldeolin.allison1875.base.process.UserInfoCollectProc;
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
        // Version
        Version.greeting();

        // 启动IOC
        Injector injector = GuiceUtils.createInjector(guiceModules);

        // 参数校验
        validate(injector, guiceModules);

        GuiceUtils.getComponent(UserInfoCollectProc.class).process(primaryClass, guiceModules);

        // 运行主流程
        launch(primaryClass, injector, guiceModules);

        // 回收资源
        destroy(injector);
    }

    private static void validate(Injector injector, Module[] guiceModules) {
        Collection<Object> components = Lists.newArrayList();
        Set<ConstraintViolation<?>> violations = Sets.newHashSet();
        for (Module guiceModule : guiceModules) {
            if (guiceModule instanceof Allison1875Module) {
                for (Class<?> supportValidationType : ((Allison1875Module) guiceModule).getSupportValidationTypes()) {
                    Object component = injector.getInstance(supportValidationType);
                    components.add(component);
                    Set<ConstraintViolation<Object>> validate = ValidateUtils.validate(component);
                    violations.addAll(validate);
                }
            }
        }
        if (violations.size() > 0) {
            log.error("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<?> violation : violations) {
                log.error(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        for (Object component : components) {
            log.info("detect config properties {}", component);
        }
    }

    private static void launch(Class<?> primaryClass, Injector injector, Module[] guiceModules) {
        for (Module guiceModule : guiceModules) {
            if (guiceModule instanceof Allison1875Module) {
                Allison1875Module allison1875Module = (Allison1875Module) guiceModule;
                AstForest astForest = new AstForest(primaryClass);
                allison1875Module.getMainProcessor(injector).process(astForest);
            }
        }
    }

    private static void destroy(Injector injector) {
        injector.getInstance(RedissonFactory.class).getRedissonClient().shutdown();
    }

}