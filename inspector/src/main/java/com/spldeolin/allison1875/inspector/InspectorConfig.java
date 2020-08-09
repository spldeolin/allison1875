package com.spldeolin.allison1875.inspector;

import java.nio.file.Path;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class InspectorConfig {

    @Getter
    private static final InspectorConfig instance = YamlUtils.toObject("inspector-config.yml", InspectorConfig.class);

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

    private InspectorConfig() {
    }

}
