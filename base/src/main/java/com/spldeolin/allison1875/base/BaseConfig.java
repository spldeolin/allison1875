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
     * Maven工程Java源码的布局（一般不需要改动此项）
     */
    @NotEmpty
    private String javaDirectoryLayout = "src/main/java";

    private BaseConfig() {
    }

    public static BaseConfig getInstance() {
        return BaseConfig.instance;
    }

    public String getJavaDirectoryLayout() {
        return this.javaDirectoryLayout;
    }

    public BaseConfig setJavaDirectoryLayout(@NotEmpty String javaDirectoryLayout) {
        this.javaDirectoryLayout = javaDirectoryLayout;
        return this;
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
