package com.spldeolin.allison1875.inspector;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Allison1875[inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Accessors(chain = true)
@Data
public class InspectorConfig {

    /**
     * 工程所在的Git本地仓库的路径
     */
    @NotNull
    protected String projectLocalGitPath;

    /**
     * 此时间之后新增的文件为靶文件
     */
    @NotNull
    protected LocalDateTime targetFileSince;

    /**
     * 周知JSON目录的路径
     */
    protected String pardonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    protected String lawlessDirectoryPath;

}
