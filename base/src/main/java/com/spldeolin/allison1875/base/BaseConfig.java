package com.spldeolin.allison1875.base;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的全局配置
 *
 * @author Deolin 2020-02-08
 */
@Data
@Log4j2
@Accessors(chain = true)
public final class BaseConfig {

    @Getter
    private static final BaseConfig instance = YamlUtils.toObject("base-config.yml", BaseConfig.class);

    static {
        instance.calcCommonPath();
    }

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime targetFileSince;

    /**
     * 项目根目录路径，此项必填
     */
    private Collection<String> projectPaths;

    /**
     * 所有projectPaths的公有部分
     */
    @JsonIgnore
    private Path commonPart;

    private BaseConfig() {
    }

    private void calcCommonPath() {
        List<String> paths = Lists.newArrayList(projectPaths);
        String common = paths.get(0);
        for (String path : paths) {
            common = Strings.commonPrefix(common, path);
        }
        commonPart = Paths.get(common);
    }

}
