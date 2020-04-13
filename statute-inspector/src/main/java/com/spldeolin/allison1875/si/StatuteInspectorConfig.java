package com.spldeolin.allison1875.si;

import java.io.File;
import java.nio.file.Path;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;

/**
 * Allison1875[statute-inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
public final class StatuteInspectorConfig {

    private static final StatuteInspectorConfig instance = new StatuteInspectorConfig();

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 周知JSON目录的路径
     */
    private Path publicAckJsonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    private Path lawlessCsvOutputDirectoryPath;

    private StatuteInspectorConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        commonPageTypeQualifier = BaseConfig.getInstace().getRawData().get("commonPageTypeQualifier");

        File publicAckJsonDirectory = new File(BaseConfig.getInstace().getRawData().get("publicAckJsonDirectoryPath"));
        if (!publicAckJsonDirectory.exists()) {
            if (!publicAckJsonDirectory.mkdirs()) {
                throw new ConfigLoadingException("Make directory failed. [" + publicAckJsonDirectory + "]");
            }
        }
        publicAckJsonDirectoryPath = publicAckJsonDirectory.toPath();

        File lawlessCsvOutputDirectory = new File(
                BaseConfig.getInstace().getRawData().get("lawlessCsvOutputDirectoryPath"));
        if (!lawlessCsvOutputDirectory.exists()) {
            if (!lawlessCsvOutputDirectory.mkdirs()) {
                throw new ConfigLoadingException("Make directory failed. [" + lawlessCsvOutputDirectory + "]");
            }
        }
        lawlessCsvOutputDirectoryPath = lawlessCsvOutputDirectory.toPath();
    }

    public static StatuteInspectorConfig getInstance() {
        return instance;
    }

}
