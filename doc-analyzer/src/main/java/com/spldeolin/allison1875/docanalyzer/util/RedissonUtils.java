package com.spldeolin.allison1875.docanalyzer.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;

/**
 * @author Deolin 2020-08-31
 */
public class RedissonUtils {

    public static RedissonClient getSingleServer() {
        Config config = new Config();
        DocAnalyzerConfig conf = DocAnalyzerConfig.getInstance();
        config.useSingleServer().setAddress(conf.getRedisAddress()).setPassword(conf.getRedisPassword());
        return Redisson.create(config);
    }

    public static void close(RedissonClient redissonClient) {
        redissonClient.shutdown();
    }

}