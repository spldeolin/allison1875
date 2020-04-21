package com.spldeolin.allison1875.base;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的全局配置
 *
 * @author Deolin 2020-02-08
 */
@Data
@Log4j2
public final class BaseConfig {

    private static final BaseConfig instace = new BaseConfig();

    /**
     * 项目根目录路径，此项必填
     */
    private Path projectPath;

    /**
     * 开启类加载时，此项必填。
     * 使用脚本前需要先确保项目已经执行过mvn clean package，然后填入打包后的jar文件或是war文件路径
     */
    private Path warOrFatJarPath;

    /**
     * 是否禁用类加载收集策略来收集CU，部分工具需要启用类加载
     * 禁用后可以加快收集速度，但是AST node将会不再支持resolved、calculateResolvedType等方法
     * （这个配置项默认为true，是否设置为false不由用户通过config.yml配置决定，而是由每个决定）
     */
    private Boolean collectWithLoadingClass = true;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime giveUpResultAddedSinceTime;

    private BaseConfig() {
        initLoad();
    }

    private void initLoad() {
        Yaml yaml = new Yaml();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("base-config.yml")) {
            Map<String, String> rawData = yaml.load(is);
            projectPath = Paths.get(rawData.get("projectPath"));
            warOrFatJarPath = Paths.get(rawData.get("warOrFatJarPath"));
            giveUpResultAddedSinceTime = TimeUtils.toLocalDateTime(rawData.get("giveUpResultAddedSinceTime"));
        } catch (Exception e) {
            log.error("BaseConfig.initLoad fail.", e);
            throw new ConfigLoadingException();
        }
    }

    public static BaseConfig getInstace() {
        return instace;
    }

}
