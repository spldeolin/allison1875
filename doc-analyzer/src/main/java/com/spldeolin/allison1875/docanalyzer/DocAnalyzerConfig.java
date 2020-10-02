package com.spldeolin.allison1875.docanalyzer;


import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
public final class DocAnalyzerConfig {

    private static final DocAnalyzerConfig instance = new DocAnalyzerConfig();

    /**
     * 目标项目handler方法签名所依赖的项目的源码路径，相对路径、绝对路径皆可
     */
    @NotNull
    private Collection<@NotEmpty String> dependencyProjectPaths = Lists.newArrayList();

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
    private String redisPassword;

    private DocAnalyzerConfig() {
    }

    public static DocAnalyzerConfig getInstance() {
        return DocAnalyzerConfig.instance;
    }

    public @NotNull Collection<@NotEmpty String> getDependencyProjectPaths() {
        return this.dependencyProjectPaths;
    }

    public String getGlobalUrlPrefix() {
        return this.globalUrlPrefix;
    }

    public @NotEmpty String getYapiUrl() {
        return this.yapiUrl;
    }

    public @NotEmpty String getYapiToken() {
        return this.yapiToken;
    }

    public @NotEmpty String getRedisAddress() {
        return this.redisAddress;
    }

    public String getRedisPassword() {
        return this.redisPassword;
    }

    public void setDependencyProjectPaths(@NotNull Collection<@NotEmpty String> dependencyProjectPaths) {
        this.dependencyProjectPaths = dependencyProjectPaths;
    }

    public void setGlobalUrlPrefix(String globalUrlPrefix) {
        this.globalUrlPrefix = globalUrlPrefix;
    }

    public void setYapiUrl(@NotEmpty String yapiUrl) {
        this.yapiUrl = yapiUrl;
    }

    public void setYapiToken(@NotEmpty String yapiToken) {
        this.yapiToken = yapiToken;
    }

    public void setRedisAddress(@NotEmpty String redisAddress) {
        this.redisAddress = redisAddress;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DocAnalyzerConfig)) {
            return false;
        }
        final DocAnalyzerConfig other = (DocAnalyzerConfig) o;
        final Object this$dependencyProjectPaths = this.getDependencyProjectPaths();
        final Object other$dependencyProjectPaths = other.getDependencyProjectPaths();
        if (this$dependencyProjectPaths == null ? other$dependencyProjectPaths != null
                : !this$dependencyProjectPaths.equals(other$dependencyProjectPaths)) {
            return false;
        }
        final Object this$globalUrlPrefix = this.getGlobalUrlPrefix();
        final Object other$globalUrlPrefix = other.getGlobalUrlPrefix();
        if (this$globalUrlPrefix == null ? other$globalUrlPrefix != null
                : !this$globalUrlPrefix.equals(other$globalUrlPrefix)) {
            return false;
        }
        final Object this$yapiUrl = this.getYapiUrl();
        final Object other$yapiUrl = other.getYapiUrl();
        if (this$yapiUrl == null ? other$yapiUrl != null : !this$yapiUrl.equals(other$yapiUrl)) {
            return false;
        }
        final Object this$yapiToken = this.getYapiToken();
        final Object other$yapiToken = other.getYapiToken();
        if (this$yapiToken == null ? other$yapiToken != null : !this$yapiToken.equals(other$yapiToken)) {
            return false;
        }
        final Object this$redisAddress = this.getRedisAddress();
        final Object other$redisAddress = other.getRedisAddress();
        if (this$redisAddress == null ? other$redisAddress != null : !this$redisAddress.equals(other$redisAddress)) {
            return false;
        }
        final Object this$redisPassword = this.getRedisPassword();
        final Object other$redisPassword = other.getRedisPassword();
        if (this$redisPassword == null ? other$redisPassword != null
                : !this$redisPassword.equals(other$redisPassword)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $dependencyProjectPaths = this.getDependencyProjectPaths();
        result = result * PRIME + ($dependencyProjectPaths == null ? 43 : $dependencyProjectPaths.hashCode());
        final Object $globalUrlPrefix = this.getGlobalUrlPrefix();
        result = result * PRIME + ($globalUrlPrefix == null ? 43 : $globalUrlPrefix.hashCode());
        final Object $yapiUrl = this.getYapiUrl();
        result = result * PRIME + ($yapiUrl == null ? 43 : $yapiUrl.hashCode());
        final Object $yapiToken = this.getYapiToken();
        result = result * PRIME + ($yapiToken == null ? 43 : $yapiToken.hashCode());
        final Object $redisAddress = this.getRedisAddress();
        result = result * PRIME + ($redisAddress == null ? 43 : $redisAddress.hashCode());
        final Object $redisPassword = this.getRedisPassword();
        result = result * PRIME + ($redisPassword == null ? 43 : $redisPassword.hashCode());
        return result;
    }

    public String toString() {
        return "DocAnalyzerConfig(dependencyProjectPaths=" + this.getDependencyProjectPaths() + ", globalUrlPrefix="
                + this.getGlobalUrlPrefix() + ", yapiUrl=" + this.getYapiUrl() + ", yapiToken=" + this.getYapiToken()
                + ", redisAddress=" + this.getRedisAddress() + ", redisPassword=" + this.getRedisPassword() + ")";
    }

}
