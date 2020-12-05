package com.spldeolin.allison1875.docanalyzer.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * @author Deolin 2020-08-31
 */
public class RedissonUtils {

    public static RedissonClient getSingleServer(String address, String password) {
        Config redissonConfig = new Config();
        SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
        singleServerConfig.setAddress(address);
        if (password != null) {
            singleServerConfig.setPassword(password);
        }
        return Redisson.create(redissonConfig);
    }

    public static void close(RedissonClient redissonClient) {
        redissonClient.shutdown();
    }

}