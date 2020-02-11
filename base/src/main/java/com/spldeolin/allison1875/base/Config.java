package com.spldeolin.allison1875.base;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.util.Times;

/**
 * @author Deolin 2020-02-08
 */
public class Config {

    /**
     * collector是否打印详细日志
     */
    private Boolean reportCollectorDetailOrNot;

    /**
     * 项目源码路径
     */
    private Path projectPath;

    /**
     * 项目打包后的war文件或是Spring Boot fat jar文件的路径
     */
    private Path warOrFatJarPath;

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 放弃位于自这个时间起新增文件的分析、检查、转化等结果
     */
    private LocalDateTime giveUpResultAddedSinceTime;

    private static Config instance = new Config();

    private Config() {
        Yaml yaml = new Yaml();
        InputStream is = null;
        try {
            is = org.apache.commons.io.FileUtils
                    .openInputStream(new java.io.File("/Users/deolin/Documents/config.yml"));
        } catch (java.io.IOException ignored) {
        }
        Map<String, String> map = yaml.load(is);
        reportCollectorDetailOrNot = BooleanUtils.toBoolean(map.get("reportCollectorDetailOrNot"));
        projectPath = Paths.get(map.get("projectPath"));
        warOrFatJarPath = Paths.get(map.get("warOrFatJarPath"));
        commonPageTypeQualifier = map.get("commonPageTypeQualifier");
        giveUpResultAddedSinceTime = Times.toLocalDateTime(map.get("giveUpResultAddedSinceTime"));
    }

    public static Boolean getReportCollectorDetailOrNot() {
        return instance.reportCollectorDetailOrNot;
    }

    public static Path getProjectPath() {
        return instance.projectPath;
    }

    public static Path getWarOrFatJarPath() {
        return instance.warOrFatJarPath;
    }

    public static String getCommonPageTypeQualifier() {
        return instance.commonPageTypeQualifier;
    }

    public static LocalDateTime getGiveUpResultAddedSinceTime() {
        return instance.giveUpResultAddedSinceTime;
    }

}
