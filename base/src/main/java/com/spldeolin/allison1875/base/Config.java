package com.spldeolin.allison1875.base;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.BooleanUtils;
import org.yaml.snakeyaml.Yaml;
import lombok.Data;

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

    private static Config instance = new Config();

    private Config() {
        Yaml yaml = new Yaml();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.yml");
        Map map = yaml.loadAs(is, Map.class);
        reportCollectorDetailOrNot = BooleanUtils.toBoolean(map.reportCollectorDetailOrNot);
        projectPath = Paths.get(map.projectPath);
        warOrFatJarPath = Paths.get(map.warOrFatJarPath);
        commonPageTypeQualifier = map.commonPageTypeQualifier;
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

    @Data
    public static class Map {

        String reportCollectorDetailOrNot;

        String projectPath;

        String warOrFatJarPath;

        String commonPageTypeQualifier;

    }

}
