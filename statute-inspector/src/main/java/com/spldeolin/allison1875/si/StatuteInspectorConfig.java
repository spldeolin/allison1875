package com.spldeolin.allison1875.si;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[statute-inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class StatuteInspectorConfig {

    private static final StatuteInspectorConfig instance = new StatuteInspectorConfig();

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 周知JSON目录的路径
     */
    private Path publicAckJsonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    private Path lawlessCsvOutputDirectoryPath;

    private StatuteInspectorConfig() {
        this.initLoad();
    }

    private void initLoad() {
        Yaml yaml = new Yaml();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("statute-inspector-config.yml")) {
            Map<String, String> rawData = yaml.load(is);
            commonPageTypeQualifier = rawData.get("commonPageTypeQualifier");

            File publicAckJsonDirectory = new File(rawData.get("publicAckJsonDirectoryPath"));
            if (!publicAckJsonDirectory.exists()) {
                if (!publicAckJsonDirectory.mkdirs()) {
                    log.error("mkdirs [{}] failed.", publicAckJsonDirectory);
                    throw new ConfigLoadingException();
                }
            }
            publicAckJsonDirectoryPath = publicAckJsonDirectory.toPath();

            File lawlessCsvOutputDirectory = new File(rawData.get("lawlessCsvOutputDirectoryPath"));
            if (!lawlessCsvOutputDirectory.exists()) {
                if (!lawlessCsvOutputDirectory.mkdirs()) {
                    log.error("mkdirs [{}] failed.", lawlessCsvOutputDirectory);
                    throw new ConfigLoadingException();
                }
            }
            lawlessCsvOutputDirectoryPath = lawlessCsvOutputDirectory.toPath();

        } catch (Exception e) {
            log.error("StatuteInspectorConfig.initLoad failed.", e);
            throw new ConfigLoadingException();
        }

    }

    public static StatuteInspectorConfig getInstance() {
        return instance;
    }

}
