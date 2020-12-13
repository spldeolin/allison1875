package com.spldeolin.allison1875.base;

import org.redisson.api.RedissonClient;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.RedissonFactory;
import com.spldeolin.allison1875.base.process.UserInfoCollectProc;
import com.spldeolin.allison1875.base.util.GuiceUtils;
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

        // 收集用户信息
        GuiceUtils.getComponent(UserInfoCollectProc.class).process(primaryClass, guiceModules);

        // 运行主流程
        launch(primaryClass, injector, guiceModules);

        // 回收资源
        destroy(injector);
    }

    private static void launch(Class<?> primaryClass, Injector injector, Module[] guiceModules) {
        boolean noneInvalid = true;
        for (Module guiceModule : guiceModules) {
            if (guiceModule instanceof Allison1875Module) {
                Allison1875Module allison1875Module = (Allison1875Module) guiceModule;
                if (allison1875Module.validateConfig(injector) && noneInvalid) {
                    AstForest astForest = new AstForest(primaryClass);
                    allison1875Module.launchMainProcessor(astForest, injector);
                } else {
                    noneInvalid = false;
                }
            }
        }
    }

    private static void destroy(Injector injector) {
        RedissonClient redissonClient = injector.getInstance(RedissonFactory.class).getRedissonClient();
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

}