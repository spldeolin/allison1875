package com.spldeolin.allison1875.base.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-10
 */
@Singleton
@Slf4j
public class RedissonFactory {

    @Inject
    private BaseConfig baseConfig;

    private RedissonClient redissonClient;

    public synchronized RedissonClient getRedissonClient() {
        if (redissonClient == null) {
            Config redissonConfig = new Config();
            SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
            if (baseConfig.getRedisAddress() == null) {
                return null;
            }
            singleServerConfig.setAddress(baseConfig.getRedisAddress());
            singleServerConfig.setPassword(baseConfig.getRedisPassword());
            redissonConfig.setCodec(new StringCodec());
            try {
                redissonClient = Redisson.create(redissonConfig);
            } catch (Exception e) {
                log.warn("无法连接到eids");
                return null;
            }
        }
        return redissonClient;
    }

}