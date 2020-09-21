package com.spldeolin.allison1875.docanalyzer.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;

/**
 * @author Deolin 2020-08-31
 */
public class RedissonUtils {

    public static RedissonClient getSingleServer() {
        Config redissonConfig = new Config();
        DocAnalyzerConfig conf = DocAnalyzerConfig.getInstance();
        SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
        singleServerConfig.setAddress(conf.getRedisAddress());
        if (conf.getRedisPassword() != null) {
            singleServerConfig.setPassword(conf.getRedisPassword());
        }
        return Redisson.create(redissonConfig);
    }

    public static void close(RedissonClient redissonClient) {
        redissonClient.shutdown();
    }

}