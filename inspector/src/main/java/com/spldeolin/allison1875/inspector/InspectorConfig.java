package com.spldeolin.allison1875.inspector;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.base.valid.annotation.IsDirectory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class InspectorConfig {

    /**
     * 工程所在的Git本地仓库的路径
     */
    @NotNull String projectLocalGitPath;

    /**
     * 此时间之后新增的文件为靶文件
     */
    @NotNull LocalDateTime targetFileSince;

    /**
     * 周知JSON目录的路径
     */
    @IsDirectory String pardonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    @IsDirectory String lawlessDirectoryPath;

}
