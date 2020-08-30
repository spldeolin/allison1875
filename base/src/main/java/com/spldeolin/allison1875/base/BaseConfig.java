package com.spldeolin.allison1875.base;

import javax.validation.constraints.NotEmpty;
import com.spldeolin.allison1875.base.util.Configs;
import com.spldeolin.allison1875.base.util.YamlUtils;
import com.spldeolin.allison1875.base.util.exception.YamlAbsentException;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的基础配置
 *
 * @author Deolin 2020-02-08
 */
@Data
@Log4j2
@Accessors(chain = true)
public final class BaseConfig {

    @Getter
    private static final BaseConfig instance = buildFromYamlMight();

    private static BaseConfig buildFromYamlMight() {
        BaseConfig result;
        try {
            result = YamlUtils.toObjectAndThen("base-config.yml", BaseConfig.class, Configs::validate);
        } catch (YamlAbsentException e) {
            return new BaseConfig();
        }
        return result;
    }

    /**
     * Maven工程Java源码的布局（一般不需要改动此项）
     */
    @NotEmpty
    private String javaDirectoryLayout = "src/main/java";

    private BaseConfig() {
    }

}
