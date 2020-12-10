package com.spldeolin.allison1875.docanalyzer;


import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Singleton
@Data
public final class DocAnalyzerConfig extends AbstractModule {

    /**
     * 目标项目handler方法签名所依赖的项目的源码路径，相对路径、绝对路径皆可
     */
    @NotNull
    protected Collection<@NotEmpty String> dependencyProjectPaths;

    /**
     * 全局URL前缀
     */
    @NotNull
    protected String globalUrlPrefix;

    /**
     * YApi请求URL
     */
    @NotEmpty
    protected String yapiUrl;

    /**
     * YApi项目的TOKEN
     */
    @NotEmpty
    protected String yapiToken;

    /**
     * Redis服务，用于作为分布式锁防止多个doc-analyzer并发同步YApi
     */
    @NotEmpty
    protected String redisAddress;

    /**
     * Redis服务的密码
     */
    protected String redisPassword;

    @Override
    protected void configure() {
        bind(DocAnalyzerConfig.class).toInstance(this);
    }

}
