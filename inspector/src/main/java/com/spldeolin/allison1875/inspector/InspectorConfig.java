package com.spldeolin.allison1875.inspector;

import java.time.LocalDateTime;
import com.spldeolin.allison1875.base.util.Configs;
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
    private static final InspectorConfig instance = YamlUtils
            .toObjectAndThen("inspector-config.yml", InspectorConfig.class, Configs::validate);

    /**
     * 工程所在的Git本地仓库的路径
     */
    private String projectLocalGitPath;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime targetFileSince;

    /**
     * 周知JSON目录的路径
     */
    private String publicAckJsonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    private String lawlessCsvOutputDirectoryPath;

    private InspectorConfig() {
    }

}
