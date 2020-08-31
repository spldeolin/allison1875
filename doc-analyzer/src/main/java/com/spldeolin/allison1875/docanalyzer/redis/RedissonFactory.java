package com.spldeolin.allison1875.docanalyzer.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;

/**
 * @author Deolin 2020-08-31
 */
public class RedissonFactory {

    public static RedissonClient getSingleServer() {
        Config config = new Config();
        DocAnalyzerConfig conf = DocAnalyzerConfig.getInstance();
        config.useSingleServer().setAddress(conf.getRedisAddress()).setPassword(conf.getRedisPassword());
        return Redisson.create(config);
    }

}