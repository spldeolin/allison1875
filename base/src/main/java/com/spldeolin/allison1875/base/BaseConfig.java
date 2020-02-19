package com.spldeolin.allison1875.base;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.Times;
import lombok.Data;

/**
 * Allison 1875的全局配置，只包含项目路径、Git增量起始时间等配置
 *
 * @author Deolin 2020-02-08
 */
@Data
public class BaseConfig {

    /**
     * 项目源码路径
     */
    private Path projectPath;

    /**
     * projectPath打包后的war文件或是Spring Boot fat jar文件的路径
     */
    private Path warOrFatJarPath;

    /**
     * 是否禁用类加载收集策略来收集CU
     * 禁用后可以加快收集速度，但是AST node将会不再支持resolved、calculateResolvedType等方法
     */
    private Boolean doNotCollectWithLoadingClass;

    /**
     * 只将这个时间后在VCS中增量的文件作为目标对象（这个时间之前新增到VCS的文件将不被处理等）
     */
    private LocalDateTime giveUpResultAddedSinceTime;

    public static final BaseConfig CONFIG = new BaseConfig();

    /**
     * 来自classpath的config.yml的原始数据
     */
    protected Map<String, String> rawData;

    protected BaseConfig() {
        initLoad();
    }

    private void initLoad() {
        Yaml yaml = new Yaml();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
            rawData = yaml.load(is);
            projectPath = Paths.get(rawData.get("projectPath"));
            warOrFatJarPath = Paths.get(rawData.get("warOrFatJarPath"));
            giveUpResultAddedSinceTime = Times.toLocalDateTime(rawData.get("giveUpResultAddedSinceTime"));
        } catch (Exception e) {
            throw new ConfigLoadingException(e);
        }
    }

}
