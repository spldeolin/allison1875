package com.spldeolin.allison1875.base;

import javax.validation.constraints.NotEmpty;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * Allison1875的基础配置
 *
 * @author Deolin 2020-02-08
 */
@Singleton
@Data
public final class BaseConfig extends AbstractModule {

    /**
     * src/main/java的相对路径
     */
    @NotEmpty
    private String javaDirectoryLayout = "src/main/java";

    /**
     * src/main/resources的相对路径
     */
    @NotEmpty
    private String resourcesDirectoryLayout = "src/main/resources";

    /**
     * src/test/java的相对路径
     */
    @NotEmpty
    private String testJavaDirectoryLayout = "src/test/java";

    /**
     * src/test/resources的相对路径
     */
    @NotEmpty
    private String testResourcesDirectoryLayout = "src/test/resources";

    /**
     * Redis服务
     */
    @NotEmpty
    private String redisAddress;

    /**
     * Redis服务的密码
     */
    private String redisPassword;

    @Override
    protected void configure() {
        bind(BaseConfig.class).toInstance(this);
    }

}
