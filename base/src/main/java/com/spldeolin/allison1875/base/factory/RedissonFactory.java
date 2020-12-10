package com.spldeolin.allison1875.base.factory;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.BaseConfig;

/**
 * @author Deolin 2020-12-10
 */
@Singleton
public class RedissonFactory {

    @Inject
    private BaseConfig baseConfig;

    private RedissonClient redissonClient;

    public synchronized RedissonClient getRedissonClient() {
        if (redissonClient == null) {
            Config redissonConfig = new Config();
            SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
            singleServerConfig.setAddress(baseConfig.getRedisAddress());
            if (baseConfig.getRedisPassword() != null) {
                singleServerConfig.setPassword(baseConfig.getRedisPassword());
            }
            redissonClient = Redisson.create(redissonConfig);
        }
        return redissonClient;
    }

}