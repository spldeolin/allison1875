package com.spldeolin.allison1875.si;

import java.io.File;
import java.nio.file.Path;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * 【statute-inspector】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Log4j2
public final class StatuteInspectorConfig extends BaseConfig {

    public static final StatuteInspectorConfig CONFIG = new StatuteInspectorConfig();

    private Path publicAckJsonDirectoryPath;

    private Path lawlessCsvOutputDirectoryPath;

    private StatuteInspectorConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        File publicAckJsonDirectory = new File(super.rawData.get("publicAckJsonDirectoryPath"));
        if (!publicAckJsonDirectory.exists()) {
            if (!publicAckJsonDirectory.mkdirs()) {
                throw new ConfigLoadingException("Make directory failed. [" + publicAckJsonDirectory + "]");
            }
        }
        publicAckJsonDirectoryPath = publicAckJsonDirectory.toPath();

        File lawlessCsvOutputDirectory = new File(super.rawData.get("lawlessCsvOutputDirectoryPath"));
        if (!lawlessCsvOutputDirectory.exists()) {
            if (!lawlessCsvOutputDirectory.mkdirs()) {
                throw new ConfigLoadingException("Make directory failed. [" + lawlessCsvOutputDirectory + "]");
            }
        }
        lawlessCsvOutputDirectoryPath = lawlessCsvOutputDirectory.toPath();
    }

}
