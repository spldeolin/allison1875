package com.spldeolin.allison1875.base.process;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.Version;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.redis.RedissonFactory;
import com.spldeolin.allison1875.base.util.GuiceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-10
 */
@Singleton
@Slf4j
public class UserInfoCollectProc {

    private static final String todayKey = "allison-1875-userInfo-" + LocalDate.now();

    @Inject
    private RedissonFactory redissonFactory;

    public void process(Class<?> primaryClass, Module... modules) {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            UserInfoDto userInfoDto = new UserInfoDto();
            userInfoDto.setHostAddress(addr.getHostAddress());
            userInfoDto.setHostName(addr.getHostName());
            userInfoDto.setUserLocation(primaryClass.getProtectionDomain().getCodeSource().getLocation().getPath());
            userInfoDto.setAllison1875Version(Version.numberCode);
            userInfoDto.setWhen(LocalDateTime.now());
            List<String> moduleNames = Lists.newArrayList();
            Map<String, Object> configs = Maps.newHashMap();
            for (Module module : modules) {
                if (module instanceof Allison1875Module) {
                    Allison1875Module allison1875Module = (Allison1875Module) module;
                    moduleNames.add(allison1875Module.getClass().getSimpleName());
                    for (Object config : allison1875Module.getConfigs(GuiceUtils.getInjector())) {
                        configs.put(config.getClass().getSimpleName(), config);
                    }
                }
            }
            userInfoDto.setModuleNames(moduleNames);
            userInfoDto.setConfigs(configs);

            saveToRedis(userInfoDto);
        } catch (Exception e) {
            log.warn("Collect user information failed. ", e);
        }
    }

    private void saveToRedis(UserInfoDto userInfoDto) {
        RAtomicLong atomicLong = redissonFactory.getRedissonClient().getAtomicLong(todayKey);
        long l = atomicLong.incrementAndGet();
        String bucketKey = todayKey + "-" + l;
        RBucket<Object> bucket = redissonFactory.getRedissonClient().getBucket(bucketKey);
        bucket.set(userInfoDto);
    }

}