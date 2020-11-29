package com.spldeolin.allison1875.base;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Allison1875的基础配置
 *
 * @author Deolin 2020-02-08
 */
@Data
public final class BaseConfig {

    private static final BaseConfig instance = new BaseConfig();

    /**
     * src/main/java的相对路径（一般不需要改动此项）
     */
    @NotEmpty
    private String javaDirectoryLayout = "src/main/java";

    /**
     * src/main/resources的相对路径（一般不需要改动此项）
     */
    @NotEmpty
    private String resourcesDirectoryLayout = "src/main/resources";

    /**
     * src/test/java的相对路径（一般不需要改动此项）
     */
    @NotEmpty
    private String testJavaDirectoryLayout = "src/test/java";

    /**
     * src/test/resources的相对路径（一般不需要改动此项）
     */
    @NotEmpty
    private String testResourcesDirectoryLayout = "src/test/resources";

    private BaseConfig() {
    }

    public static BaseConfig getInstance() {
        return BaseConfig.instance;
    }

}
