package com.spldeolin.allison1875.docanalyzer;


import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.ConfigUtils;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class DocAnalyzerConfig {

    @Getter
    private static final DocAnalyzerConfig instance = YamlUtils
            .toObjectAndThen("doc-analyzer-config.yml", DocAnalyzerConfig.class, ConfigUtils::validate);

    /**
     * 目标项目handler方法签名所依赖的项目的源码路径
     */
    @NotNull
    private Collection<@NotEmpty String> dependencyProjectPaths = Lists.newArrayList();

    /**
     * 根据作者名过滤
     */
    private String filterByAuthorName;

    /**
     * 全局URL前缀
     */
    private String globalUrlPrefix;

    /**
     * YApi请求URL
     */
    @NotEmpty
    private String yapiUrl;

    /**
     * YApi项目的TOKEN
     */
    @NotEmpty
    private String yapiToken;

    /**
     * Redis服务，用于作为分布式锁防止多个doc-analyzer并发同步YApi
     */
    @NotEmpty
    private String redisAddress;

    /**
     * Redis服务的密码
     */
    @NotEmpty
    private String redisPassword;

    private DocAnalyzerConfig() {
    }

}
