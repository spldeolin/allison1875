package com.spldeolin.allison1875.si;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【statute-inspector】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class StatuteInspectorConfig extends BaseConfig {

    public static final StatuteInspectorConfig CONFIG = new StatuteInspectorConfig();

    private Path lawlessCsvOutputDirectoryPath;

    private StatuteInspectorConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        File lawlessCsvOutputDirectory = new File(super.rawData.get("lawlessCsvOutputDirectoryPath"));
        if (!lawlessCsvOutputDirectory.exists()) {
            if (!lawlessCsvOutputDirectory.mkdirs()) {
                throw new ConfigLoadingException("文件" + lawlessCsvOutputDirectory + "创建失败");
            }
        }
        lawlessCsvOutputDirectoryPath = Paths.get(super.rawData.get("lawlessCsvOutputDirectoryPath"));
    }

}
