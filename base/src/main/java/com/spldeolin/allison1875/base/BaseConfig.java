package com.spldeolin.allison1875.base;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.JsonUtils;
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

    @Getter
    private static final BaseConfig instance = createInstance();

    private BaseConfig() {
    }

    private static BaseConfig createInstance() {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        JsonUtils.initObjectMapper(om);
        BaseConfig baseConfig;
        try {
            InputStream inputStream = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("base-config.yml"),
                    "base-config.yml not exist.");
            baseConfig = om.readValue(inputStream, BaseConfig.class);
        } catch (IOException e) {
            log.error("读取配置文件失败：{}", e.getMessage());
            throw new ConfigLoadingException();
        }

        baseConfig.getProjectPaths().removeIf(Objects::isNull);
        if (baseConfig.getProjectPaths().size() == 0) {
            log.error("未指定projectPath");
            throw new ConfigLoadingException();
        }

        baseConfig.calcCommonPath();
        return baseConfig;
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
