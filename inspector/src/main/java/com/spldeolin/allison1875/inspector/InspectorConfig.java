package com.spldeolin.allison1875.inspector;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.ValidUtils;
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
public final class InspectorConfig extends Allison1875Config {

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
    File pardonDirectory;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    File lawlessDirectory;

    @Override
    public List<InvalidDto> invalidSelf() {
        List<InvalidDto> invalids = super.invalidSelf();
        if (pardonDirectory != null) {
            if (!pardonDirectory.exists() || !pardonDirectory.isDirectory()) {
                invalids.add(
                        new InvalidDto().setPath("pardonDirectory").setValue(ValidUtils.formatValue(pardonDirectory))
                                .setReason("must exist and be a directory"));
            }
        }
        if (lawlessDirectory != null) {
            if (!lawlessDirectory.exists() || !lawlessDirectory.isDirectory()) {
                invalids.add(
                        new InvalidDto().setPath("lawlessDirectory").setValue(ValidUtils.formatValue(lawlessDirectory))
                                .setReason("must exist and be a directory"));
            }
        }
        return invalids;
    }

}
