package com.spldeolin.allison1875.da.deprecated;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[doc-analyzer]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class DocAnalyzerConfig {

    /**
     * Maven打包的命令行
     */
    private String mavenPackageCommandLine;

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 文档输出路径
     */
    private Path docOutputDirectoryPath;

    public static final DocAnalyzerConfig CONFIG = new DocAnalyzerConfig();

    private DocAnalyzerConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        Yaml yaml = new Yaml();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("doc-analyzer-config.yml")) {
            Map<String, String> rawData = yaml.load(is);

            mavenPackageCommandLine = rawData.get("mavenPackageCommandLine");

            commonPageTypeQualifier = rawData.get("commonPageTypeQualifier");

            File docOutputDirectory = new File(rawData.get("docOutputDirectoryPath"));
            if (!docOutputDirectory.exists()) {
                if (!docOutputDirectory.mkdirs()) {
                    log.error("[{}] mkdir failed.", docOutputDirectory);
                    throw new ConfigLoadingException();
                }
            }
            docOutputDirectoryPath = docOutputDirectory.toPath();

        } catch (Exception e) {
            log.error("DocAnalyzerConfig.initLoad failed.", e);
            throw new ConfigLoadingException();
        }

    }

}
