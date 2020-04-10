package com.spldeolin.allison1875.base;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.Data;

/**
 * Allison 1875的全局配置，只包含项目路径、Git增量起始时间等配置
 *
 * @author Deolin 2020-02-08
 */
@Data
public class BaseConfig {

    /**
     * 填写项目根目录路径，此项必填
     */
    private Path projectPath;

    /**
     * 开启类加载时，此项必填。需要先对项目进行clean package，然后填入打包后的jar文件或是war文件路径
     */
    private Path warOrFatJarPath;

    /**
     * 是否禁用类加载收集策略来收集CU，部分工具需要启用类加载
     * 禁用后可以加快收集速度，但是AST node将会不再支持resolved、calculateResolvedType等方法
     */
    private Boolean doNotCollectWithLoadingClass;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
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
            doNotCollectWithLoadingClass = BooleanUtils.toBoolean(rawData.get("doNotCollectWithLoadingClass"));
            giveUpResultAddedSinceTime = TimeUtils.toLocalDateTime(rawData.get("giveUpResultAddedSinceTime"));
        } catch (Exception e) {
            throw new ConfigLoadingException(e);
        }
    }

}
