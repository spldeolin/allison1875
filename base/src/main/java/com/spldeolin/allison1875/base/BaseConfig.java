package com.spldeolin.allison1875.base;

import javax.validation.constraints.NotEmpty;
import org.apache.logging.log4j.Logger;

/**
 * Allison1875的基础配置
 *
 * @author Deolin 2020-02-08
 */
public final class BaseConfig {

    private static final BaseConfig instance = new BaseConfig();

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(BaseConfig.class);

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

    public String getJavaDirectoryLayout() {
        return javaDirectoryLayout;
    }

    public void setJavaDirectoryLayout(String javaDirectoryLayout) {
        this.javaDirectoryLayout = javaDirectoryLayout;
    }

    public String getResourcesDirectoryLayout() {
        return resourcesDirectoryLayout;
    }

    public void setResourcesDirectoryLayout(String resourcesDirectoryLayout) {
        this.resourcesDirectoryLayout = resourcesDirectoryLayout;
    }

    public String getTestJavaDirectoryLayout() {
        return testJavaDirectoryLayout;
    }

    public void setTestJavaDirectoryLayout(String testJavaDirectoryLayout) {
        this.testJavaDirectoryLayout = testJavaDirectoryLayout;
    }

    public String getTestResourcesDirectoryLayout() {
        return testResourcesDirectoryLayout;
    }

    public void setTestResourcesDirectoryLayout(String testResourcesDirectoryLayout) {
        this.testResourcesDirectoryLayout = testResourcesDirectoryLayout;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaseConfig)) {
            return false;
        }
        final BaseConfig other = (BaseConfig) o;
        final Object this$javaDirectoryLayout = this.getJavaDirectoryLayout();
        final Object other$javaDirectoryLayout = other.getJavaDirectoryLayout();
        return this$javaDirectoryLayout == null ? other$javaDirectoryLayout == null
                : this$javaDirectoryLayout.equals(other$javaDirectoryLayout);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $javaDirectoryLayout = this.getJavaDirectoryLayout();
        result = result * PRIME + ($javaDirectoryLayout == null ? 43 : $javaDirectoryLayout.hashCode());
        return result;
    }

    public String toString() {
        return "BaseConfig(javaDirectoryLayout=" + this.getJavaDirectoryLayout() + ")";
    }

}
